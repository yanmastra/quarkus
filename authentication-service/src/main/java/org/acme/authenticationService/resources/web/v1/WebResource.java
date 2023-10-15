package org.acme.authenticationService.resources.web.v1;

import com.acme.authorization.json.SignInCredential;
import com.acme.authorization.security.UserPrincipal;
import com.acme.authorization.utils.CacheUpdateMode;
import com.acme.authorization.utils.Constants;
import com.acme.authorization.utils.DateTimeUtils;
import com.acme.authorization.utils.KeyValueCacheUtils;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.*;
import org.acme.authenticationService.dao.ApplicationJson;
import org.acme.authenticationService.dao.web.ApplicationModel;
import org.acme.authenticationService.dao.web.Home;
import org.acme.authenticationService.dao.web.Login;
import org.acme.authenticationService.resources.web.WebUtils;
import org.acme.authenticationService.services.AuthenticationService;
import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.*;

@Path("web/v1")
public class WebResource {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance login(Login data);
        public static native TemplateInstance home(Home data);
        public static native TemplateInstance applications(ApplicationModel data);
    }

    @Inject
    Logger logger;
    @ConfigProperty(name = "application-name", defaultValue = "Example App")
    String appName;
    @Inject
    AuthenticationService authService;
    @Inject WebService webService;

    @GET
    @Path("signout")
    public Uni<Response> signout() {
        return Uni.createFrom().item(WebUtils.createRedirectResponse(null, "/web/v1/auth", Collections.singleton(HttpHeaders.AUTHORIZATION))
                .build());
    }

    private String getLoginId(ContainerRequestContext context) {
        String loginId = null;
        if (context.getCookies() != null) {
            Cookie cookie = context.getCookies().get("login_id");
            if (cookie != null) {
                loginId = cookie.getValue();
                Date exp = DateTimeUtils.fromUtc(KeyValueCacheUtils.findCache(Constants.SIGN_IN_FORM_EXP, loginId));
                if (exp != null && exp.before(new Date())) {
                    KeyValueCacheUtils.saveCache(Constants.SIGN_IN_FORM_EXP, loginId, null, CacheUpdateMode.REMOVE);
                    loginId = null;
                }
            }
        }
        if (loginId == null) {
            loginId = UUID.randomUUID().toString();
        }
        return loginId;
    }

    @PermitAll
    @GET
    @Path("auth")
    @Produces(MediaType.TEXT_HTML)
    public Uni<Response> auth(@Context ContainerRequestContext context) {
        if (context.getSecurityContext() != null && context.getSecurityContext().getUserPrincipal() != null) {
            return Uni.createFrom().item(WebUtils.createRedirectResponse(null, "/web/v1/home").build());
        }

        String loginId = getLoginId(context);

        KeyValueCacheUtils.saveCache(Constants.SIGN_IN_FORM_EXP, loginId, DateTimeUtils.formattedUtcDate(DateTimeUtils.getExpiredRefreshToken()), CacheUpdateMode.ADD);

        Login login = WebUtils.createModel(new Login(), appName);
        login.loginId = loginId;

        logger.info(context.getUriInfo().getPath()+": cookie:"+WebUtils.logCookies(context));

        if (context.getCookies() != null && context.getCookies().containsKey(Constants.COOKIE_MESSAGES)) {
            login.errorMessage = context.getCookies().get(Constants.COOKIE_MESSAGES).getValue();
        }
        return Uni.createFrom().item(WebUtils.createOkResponse(
                Collections.singletonMap("LoginID", loginId),
                Templates.login(login),
                Collections.singleton(Constants.COOKIE_MESSAGES)
        ).build());
    }

    @PermitAll
    @POST
    @Path("auth")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("*/*")
    public Uni<Response> signIn(@BeanParam SignInCredential credential, @Context ContainerRequestContext context) {
        String loginId = getLoginId(context);

        credential.appCode = "SYSTEM";
        Date exp = DateUtils.addDays(new Date(), 1);
        exp = DateUtils.setHours(exp, 0);
        exp = DateUtils.setMinutes(exp, 0);
        exp = DateUtils.setSeconds(exp, 0);
        exp = DateUtils.setMilliseconds(exp, 0);
        credential.expToken = exp;

        return authService.authenticate(credential).onItem().transform(r -> WebUtils.createOkResponse(
                Collections.singletonMap(HttpHeaders.AUTHORIZATION, r.accessToken),
                Templates.login(new Login(null, null, true, null))
        ).build()).onFailure().recoverWithItem(e -> {
            logger.error("error:"+e.getMessage());
            return WebUtils.createRedirectResponse(Collections.singletonMap(Constants.COOKIE_MESSAGES, "Invalid credential"), "/web/v1/home")
                    .build();
        });
    }

    @GET
    @Path("home")
    @RolesAllowed({"VIEW_ALL"})
    public Uni<Response> home(@Context SecurityContext context) {
        logger.info("SecurityContext:"+context.getClass().getName());
        return webService.getHomePageData(UserPrincipal.valueOf(context))
                .map(home -> Response.ok(Templates.home(home)).build());
    }

    @GET
    @Path("applications")
    @RolesAllowed({"VIEW_ALL"})
    public Uni<Response> applications(@QueryParam("search") String search, @Context ContainerRequestContext context) {
        logger.info("SecurityContext:"+context.getClass().getName());
        return webService.getApplicationModel(1, 20, search, UserPrincipal.valueOf(context))
                .map(app -> {
                    Set<String> remove = new HashSet<>();
                    Map<String, Cookie> cookieMap = context.getCookies();
                    if (cookieMap != null) {
                        String msg = cookieMap.get(Constants.COOKIE_MESSAGES).getValue();
                        boolean success = "true".equals(cookieMap.get(Constants.COOKIE_MESSAGES_SUCCESS).getValue());
                        remove.add(Constants.COOKIE_MESSAGES);
                        remove.add(Constants.COOKIE_MESSAGES_SUCCESS);

                        app.isAlert = true;
                        app.isAlertSuccess = success;
                        app.alertMessage = msg;
                    }
                    return WebUtils.createOkResponse(null, Templates.applications(app), remove).build();
                });
    }

    @POST
    @Path("applications")
    @RolesAllowed({"CREATE_APP"})
    public Uni<Response> createApplication(ApplicationJson app, @Context ContainerRequestContext context) {
        return webService.createApp(app, UserPrincipal.valueOf(context))
                .map(result -> {
                    String message;
                    if (result) message = "Application has been created";
                    else message = "Failed to save application";

                    Map<String, String> messages = new HashMap<>();
                    messages.put(Constants.COOKIE_MESSAGES, message);
                    messages.put(Constants.COOKIE_MESSAGES_SUCCESS, result+"");
                    return WebUtils.createRedirectResponse(messages, context.getUriInfo().getPath()).build();
                });
    }


}
