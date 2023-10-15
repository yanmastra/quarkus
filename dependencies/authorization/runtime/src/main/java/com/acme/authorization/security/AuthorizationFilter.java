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
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.stream.Stream;

@PreMatching
@Provider
public class AuthorizationFilter implements ContainerRequestFilter {
    @Inject
    ObjectMapper objectMapper;
    @ConfigProperty(name = "authorization.public-path", defaultValue = "")
    String publicPath;
    @ConfigProperty(name = "authorization.service-url", defaultValue = "")
    String authorizationUrl;
    @ConfigProperty(name = "authorization.default-redirect", defaultValue = "")
    String defaultRedirect;

    @Inject
    Logger logger;

    @Inject
    Instance<Authorizer> authorizerInstance;

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        String userAgent = "";
        try {
            userAgent = context.getHeaders().getFirst(HttpHeaderNames.USER_AGENT.toString());
        } catch (Exception e){
            logger.error(e.getMessage());
        }
        String accept = context.getHeaders().getFirst(HttpHeaders.ACCEPT);
        String auth = context.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (StringUtil.isNullOrEmpty(auth) && context.getCookies() != null) {
            Cookie cookie = context.getCookies().get(HttpHeaders.AUTHORIZATION);
            if (cookie != null && !StringUtil.isNullOrEmpty(cookie.getValue())) {
                logger.info("authorizing: cookie available");
                auth = cookie.getValue();
            }
        }

        if (StringUtil.isNullOrEmpty(auth)) {
            if (isPublic(context, publicPath) || defaultRedirect.equals(context.getUriInfo().getPath())) {
                context.setSecurityContext(new UserSecurityContext());
                return;
            }

            if (!accept.contains(MediaType.APPLICATION_JSON)) {
                URI uri = URI.create(defaultRedirect);
                logger.info("authorizing:" + context.getUriInfo().getPath()+", from:"+userAgent+": false by accept type");
                context.abortWith(Response.temporaryRedirect(uri).build());
                return;
            }
        }

        if (!StringUtil.isNullOrEmpty(auth)) {
            if (auth.startsWith("Bearer ")) auth = auth.replace("Bearer ", "");
        }

        try {
            UserPrincipal principal = authorize(auth);
            logger.info("authorizing:" + context.getUriInfo().getPath()+", from:"+userAgent+": true");
            context.setSecurityContext(new UserSecurityContext(principal));
            return;
        }catch (Throwable e) {
            logger.error(e.getMessage(), e);
            if (e instanceof HttpException httpException) {
                logger.info("authorizing:" + context.getUriInfo().getPath()+", from:"+userAgent+": false by token");
                context.abortWith(Response.status(httpException.getStatusCode()).entity(new ResponseJson<>(false, httpException.getPayload())).build());
                return;
            }
        }

        logger.info("authorizing:" + context.getUriInfo().getPath()+", from:"+userAgent+": false auth service access");
        context.abortWith(Response.status(HttpResponseStatus.BAD_GATEWAY.code()).entity(new ResponseJson<>(false,"Unable to access authenticate/authorize server")).build());
    }

    private UserPrincipal authorize(String accessToken) {
        TokenType type = checkType(accessToken);

        if (type == TokenType.DEFAULT) {
            Authorizer authorizerPrior = null;

            try (Stream<Authorizer> authorizerStream = authorizerInstance.stream()){
                authorizerPrior = authorizerStream.findFirst().orElse(null);
            } catch (Exception e){
                //
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
