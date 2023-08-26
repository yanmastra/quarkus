package com.acme.authorization.security;

import com.acme.authorization.json.ResponseJson;
import com.acme.authorization.utils.UriMatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.runtime.util.StringUtil;
import io.vertx.ext.web.handler.HttpException;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;

@PreMatching
@Provider
public class AuthorizationFilter implements ContainerRequestFilter {
    @Inject
    ObjectMapper objectMapper;
    @ConfigProperty(name = "authorization.public-path", defaultValue = "")
    String publicPath;
    @ConfigProperty(name = "authorization.service-url", defaultValue = "")
    String authorizationUrl;
    @Inject
    Logger logger;

    @Inject
    Instance<Authorizer> authorizerInstance;

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        if (isPublic(context, publicPath)) {
            context.setSecurityContext(new UserSecurityContext());
            return;
        }

        logger.info("authorizing:"+context.getUriInfo().getPath());
        String auth = context.getHeaders().getFirst(HttpHeaderNames.AUTHORIZATION.toString());
        if (!StringUtil.isNullOrEmpty(auth)) {
            if (auth.startsWith("Bearer ")) auth = auth.replace("Bearer ", "");
        }

        try {
            UserPrincipal principal = authorize(auth);
            context.setSecurityContext(new UserSecurityContext(principal));
            return;
        }catch (Throwable e) {
            logger.error(e.getMessage(), e);
            if (e instanceof HttpException httpException) {
                context.abortWith(Response.status(httpException.getStatusCode()).entity(new ResponseJson<>(false, httpException.getPayload())).build());
                return;
            }
        }
        context.abortWith(Response.status(HttpResponseStatus.BAD_GATEWAY.code()).entity(new ResponseJson<>(false,"Unable to access authenticate/authorize server")).build());
    }

    private UserPrincipal authorize(String accessToken) {
        TokenType type = checkType(accessToken);

        if (type == TokenType.DEFAULT) {
            Authorizer authorizerPrior = null;
            if (authorizerInstance.stream().count() > 1) {
                for (Authorizer authorizer: authorizerInstance) {
                    authorizerPrior = authorizer;
                    break;
                }
            } else  {
                authorizerPrior = authorizerInstance.stream().findFirst().orElse(null);
            }
            if (authorizerPrior == null) {
                authorizerPrior = new HttpAuthorizer.Builder()
                        .setObjectMapper(objectMapper)
                        .setUrl(authorizationUrl)
                        .build();
            }

            return authorizerPrior.authorize(accessToken);
        } else {
            throw new HttpException(HttpResponseStatus.BAD_REQUEST.code(), "Token type is not supported yet");
        }
    }

    private boolean isPublic(ContainerRequestContext context, String publicPath) {
        String[] publicPaths = publicPath.split(",");
        return UriMatcher.isMatch(publicPaths, context.getUriInfo().getPath());
    }

    private TokenType checkType(String accessToken) {
        if (StringUtil.isNullOrEmpty(accessToken)) throw new HttpException(HttpResponseStatus.BAD_REQUEST.code(), "Token not provided");
        if (accessToken.startsWith("GOOGLE.")) {
            return TokenType.GOOGLE;
        } else {
            return TokenType.DEFAULT;
        }
    }
}
