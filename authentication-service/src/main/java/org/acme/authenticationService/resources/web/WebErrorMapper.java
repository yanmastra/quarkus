package org.acme.authenticationService.resources.web;

import com.acme.authorization.provider.HtmlErrorMapper;
import com.acme.authorization.security.UserPrincipal;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.vertx.ext.web.handler.HttpException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.acme.authenticationService.dao.web.ErrorModel;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@Singleton
public class WebErrorMapper implements HtmlErrorMapper {
    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance notFound(ErrorModel data);
    }

    @ConfigProperty(name = "application-name", defaultValue = "Example App")
    String appName;

    @Inject
    ContainerRequestContext requestContext;

    @Inject
    Logger logger;

    @Override
    public Response getResponse(Throwable e) {
        logger.warn("Handling error:"+e.getMessage(), e);
        int status = 500;
        String messages = e.getMessage();
        if (e instanceof HttpException httpException) {
            status = httpException.getStatusCode();
        } else if (e instanceof WebApplicationException webApplicationException) {
            status = webApplicationException.getResponse().getStatus();
        } else if (e instanceof SecurityException) {
            status = 403;
        }

        ErrorModel error = new ErrorModel(status, messages);

        try {
            MultivaluedMap<String, String> params = requestContext.getUriInfo().getQueryParameters();
            String redirect = params.getFirst("redirect");
            if (StringUtils.isNotBlank(redirect))
                error.redirect = redirect;
        } catch (Exception ex){
            logger.warn(ex.getMessage());
        }

        try {
            error.user = (UserPrincipal.valueOf(requestContext.getSecurityContext().getUserPrincipal())).getUser();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return Response.status(status).entity(Templates.notFound(error)).build();
    }
}
