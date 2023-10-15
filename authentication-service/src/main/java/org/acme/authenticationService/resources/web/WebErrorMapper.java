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
import jakarta.ws.rs.core.Response;
import org.acme.authenticationService.dao.web.ErrorModel;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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

    @Override
    public Response getResponse(Throwable e) {
        if (e instanceof HttpException httpException) {
            ErrorModel error = WebUtils.createModel(new ErrorModel(httpException.getStatusCode(), httpException.getMessage()), appName);
            error.user = (UserPrincipal.valueOf(requestContext.getSecurityContext().getUserPrincipal())).getUser();
            return Response.status(httpException.getStatusCode())
                    .entity(Templates.notFound(error))
                    .build();
        } else if (e instanceof WebApplicationException webApplicationException) {
            ErrorModel error = WebUtils.createModel(new ErrorModel(webApplicationException.getResponse().getStatus(), webApplicationException.getMessage()), appName);
            error.user = (UserPrincipal.valueOf(requestContext.getSecurityContext().getUserPrincipal())).getUser();
            return Response.status(webApplicationException.getResponse().getStatus())
                    .entity(Templates.notFound(error))
                    .build();
        } else {
            ErrorModel error = WebUtils.createModel(new ErrorModel(500, e.getMessage()), appName);
            error.user = (UserPrincipal.valueOf(requestContext.getSecurityContext().getUserPrincipal())).getUser();
            return Response.status(500)
                    .entity(Templates.notFound(error))
                    .build();
        }
    }
}
