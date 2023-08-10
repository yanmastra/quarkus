package com.acme.authorization.security;

import com.acme.authorization.json.UserOnly;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.quarkus.logging.Log;
import io.quarkus.runtime.util.StringUtil;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.vertx.ext.web.handler.HttpException;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@PreMatching
@Provider
public class AuthorizationFilter implements ContainerRequestFilter {

    @ConfigProperty(name = "authorization.security-secret-key", defaultValue = "")
    String securitySecretKey;
    @Inject
    JWTParser parser;
    @Inject
    ObjectMapper objectMapper;
    @ConfigProperty(name = "authorization.public-path", defaultValue = "")
    String publicPath;
    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        if ( publicPath.contains(context.getUriInfo().getPath())) {
            return;
        }

        Log.info("authorizing:"+context.getUriInfo().getPath());
        String auth = context.getHeaders().getFirst(HttpHeaderNames.AUTHORIZATION.toString());
        if (!StringUtil.isNullOrEmpty(auth)) {
            if (auth.startsWith("Bearer ")) auth = auth.replace("Bearer ", "");
        } else {
            return;
        }

        try {
            JsonWebToken jsonWebToken = parser.decrypt(auth, securitySecretKey);
            String subject = jsonWebToken.getSubject();

            UserOnly userOnly = objectMapper.readValue(subject, UserOnly.class);

            List<String> roles = new ArrayList<>(jsonWebToken.getGroups());
            context.setSecurityContext(new UserSecurityContext(new UserPrincipal(userOnly, roles)));
            Log.info("authorized:"+context.getUriInfo().getPath());
        } catch (ParseException | JsonProcessingException e) {
            Log.info("authorizing:"+context.getUriInfo().getPath());
            Log.error(e.getMessage(), e);
            throw new HttpException(401, e.getMessage(), e);
        }
    }
}
