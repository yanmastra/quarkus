package com.acme.authorization.security;

import com.acme.authorization.json.ResponseJson;
import com.acme.authorization.utils.JsonUtils;
import com.acme.authorization.utils.UriMatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.util.StringUtil;
import io.vertx.ext.web.handler.HttpException;
import jakarta.annotation.Priority;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.stream.Stream;

@Priority(0)
@PreMatching
@Singleton
public class AuthorizationFilter implements ContainerRequestFilter {
    @Inject
    ObjectMapper objectMapper;
    @ConfigProperty(name = "authorization.public-path", defaultValue = "")
    String publicPath;
    @ConfigProperty(name = "authorization.service-url", defaultValue = "")
    String authorizationUrl;
    @ConfigProperty(name = "authorization.default-redirect", defaultValue = "")
    String defaultRedirect;

    void onStart(@Observes StartupEvent event) {
        JsonUtils.setObjectMapper(objectMapper);
    }

    @Inject
    Logger logger;

    @Inject
    Instance<Authorizer> authorizerInstance;

    private Authorizer defaultAuthorizerPrior = null;

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        UserSecurityContext securityContext = new UserSecurityContext();

        String auth = context.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        AuthType type = AuthType.AUTHORIZATION;

        if (StringUtil.isNullOrEmpty(auth) && context.getCookies() != null) {
            Cookie cookie = context.getCookies().get(HttpHeaders.AUTHORIZATION);
            if (cookie != null && !StringUtil.isNullOrEmpty(cookie.getValue())) {
                auth = cookie.getValue();
                type = AuthType.COOKIE;
            }
        }

        MultivaluedMap<String, String> params = context.getUriInfo().getQueryParameters();
        if (StringUtil.isNullOrEmpty(auth)) {
            String key = params.getFirst("key");

            if (StringUtil.isNullOrEmpty(key)) {
                key = context.getUriInfo().getQueryParameters().getFirst("API_KEY");
            }

            if (!StringUtil.isNullOrEmpty(key)) {
                auth = key;
                type = AuthType.API_KEY;
            }
        }

        logger.debug("Validating:"+context.getMethod()+" --> "+context.getUriInfo().getRequestUri()+", Auth:"+(!StringUtil.isNullOrEmpty(auth))+", type:"+type+", public:"+isPublic(context, publicPath));

        if (!isPublic(context, publicPath) && StringUtil.isNullOrEmpty(auth)) {
            this.resolveFail(context, securityContext, new HttpException(HttpResponseStatus.BAD_REQUEST.code(), "Token not provided!"));
            return;
        }

        if (!StringUtil.isNullOrEmpty(auth)) {
            try {
                if (auth.startsWith("Bearer ")) auth = auth.replace("Bearer ", "");
                UserPrincipal principal = authorize(auth, type);
                securityContext = new UserSecurityContext(principal);
                logger.debug("Valid:"+context.getMethod()+" --> "+context.getUriInfo().getRequestUri()+" user:"+principal.getUser().getEmail());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                if (!isPublic(context, publicPath)) {
                    resolveFail(context, securityContext, e);
                    return;
                }
            }
        }

        context.setSecurityContext(securityContext);
    }

    private void resolveFail(ContainerRequestContext context, SecurityContext securityContext, Throwable throwable) {
        String userAgent = context.getHeaders().getFirst(HttpHeaders.USER_AGENT);
        String accept = context.getHeaders().getFirst(HttpHeaders.ACCEPT);

        if (isPublic(context, publicPath) ||
                (isPublic(defaultRedirect, publicPath) && defaultRedirect.equalsIgnoreCase(context.getUriInfo().getPath()))) {
            context.setSecurityContext(securityContext);
            return;
        }

        if ((StringUtil.isNullOrEmpty(accept) || !accept.contains(MediaType.APPLICATION_JSON)) &&
                !defaultRedirect.equalsIgnoreCase(context.getUriInfo().getPath())) {
            logger.info("authorized:" + context.getUriInfo().getPath()+", from:"+userAgent+": false by accept type");
            URI uri = URI.create(defaultRedirect);
            context.abortWith(Response.temporaryRedirect(uri).build());
            return;
        }

        if (throwable instanceof HttpException httpException) {
            logger.info("authorized:" + context.getUriInfo().getPath()+", from:"+userAgent+": false by token");
            context.abortWith(Response.status(httpException.getStatusCode()).entity(new ResponseJson<>(false, httpException.getPayload())).build());
            return;
        }

        if (throwable instanceof ClientErrorException clientException) {
            logger.info("authorized:" + context.getUriInfo().getPath()+", from:"+userAgent+": false by token");
            context.abortWith(Response.status(clientException.getResponse().getStatus()).entity(new ResponseJson<>(false, clientException.getMessage())).build());
            return;
        }

        if (throwable != null) {
            logger.info("authorized:" + context.getUriInfo().getPath()+", from:"+userAgent+": false by exception:"+throwable.getMessage());
            context.abortWith(Response.status(500).entity(new ResponseJson<>(false, throwable.getMessage())).build());
            return;
        }

        logger.info("authorized:" + context.getUriInfo().getPath()+", from:"+userAgent+": false by unknown problem");
        context.abortWith(Response.status(HttpResponseStatus.BAD_GATEWAY.code()).entity(new ResponseJson<>(false,"Unknown problem")).build());
    }

    private UserPrincipal authorize(String accessToken, AuthType authType) {
        Authorizer authorizerPrior = null;

        try (Stream<Authorizer> authorizerStream = authorizerInstance.stream()){
            authorizerPrior = authorizerStream.findFirst().orElse(null);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
        }

        if (authorizerPrior == null) {
            authorizerPrior = getDefaultAuthorizerPrior();
        }

        if (authorizerPrior instanceof BaseAuthorizer baseAuthorizer) {
            return baseAuthorizer.authorize(accessToken);
        } else return authorizerPrior.authorize(accessToken);
    }

    private Authorizer getDefaultAuthorizerPrior() {
        if (defaultAuthorizerPrior == null) {
            defaultAuthorizerPrior = new HttpAuthorizer.Builder()
                    .setObjectMapper(objectMapper)
                    .setUrl(authorizationUrl)
                    .isShowErrorLog(true)
                    .build();
        }
        return defaultAuthorizerPrior;
    }

    private boolean isPublic(ContainerRequestContext context, String publicPathMatcher) {
        return isPublic(context.getUriInfo().getPath(), publicPathMatcher);
    }

    private boolean isPublic(String path, String publicPathMatcher) {
        if (StringUtil.isNullOrEmpty(publicPathMatcher))
            throw new RuntimeException("Parameter 'publicPathMatcher' is not specified!");

        if (path.equals(publicPathMatcher)) return true;
        if ("/*".equals(publicPathMatcher)) return true;
        String[] publicPaths = publicPathMatcher.split(",");
        return UriMatcher.isMatch(publicPaths, path);
    }
}
