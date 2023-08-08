package com.acme.authorization.security;

import com.acme.authorization.json.Permission;
import com.acme.authorization.json.RoleWithPermission;
import com.acme.authorization.json.UserOnly;
import com.acme.authorization.json.UserWithPermission;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.quarkus.logging.Log;
import io.quarkus.runtime.util.StringUtil;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.vertx.ext.web.handler.HttpException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.server.spi.ResteasyReactiveContainerRequestContext;
import org.jboss.resteasy.reactive.server.spi.ResteasyReactiveContainerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AuthorizationFilter implements ResteasyReactiveContainerRequestFilter {

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
        if (publicPath.contains(context.getUriInfo().getPath())) {
            return;
        }

        Log.info("authorizing:"+context.getUriInfo().getPath());
        String auth = context.getHeaders().getFirst(HttpHeaderNames.AUTHORIZATION.toString());
        if (!StringUtil.isNullOrEmpty(auth)) {
            if (auth.startsWith("Bearer ")) auth = auth.replace("Bearer ", "");
        }

        try {
            JsonWebToken jsonWebToken = parser.decrypt(auth, securitySecretKey);
            String subject = jsonWebToken.getSubject();
            UserWithPermission payload = objectMapper.readValue(subject, UserWithPermission.class);

            UserOnly userOnly = new UserOnly(payload.getId(), payload.getUsername(), payload.getEmail(), payload.getName());
            List<String> roles = new ArrayList<>();
            for (RoleWithPermission rp: payload.getRoles()) {
                if (rp.getAppCode().equals(jsonWebToken.getIssuer())) {
                    for (Permission p: rp.getPermissions()) {
                        roles.add(p.getCode());
                    }
                }
            }
            context.setSecurityContext(new UserSecurityContext(new UserPrincipal(userOnly, roles)));
            Log.info("authorized:"+context.getUriInfo().getPath());
        } catch (ParseException | JsonProcessingException e) {
            Log.info("authorizing:"+context.getUriInfo().getPath());
            Log.error(e.getMessage(), e);
            throw new HttpException(401, e.getMessage(), e);
        }
    }

    @Override
    public void filter(ResteasyReactiveContainerRequestContext requestContext) {
        Log.info("ResteasyReactiveContainerRequestContext::"+requestContext);
        requestContext.resume();
    }


}
