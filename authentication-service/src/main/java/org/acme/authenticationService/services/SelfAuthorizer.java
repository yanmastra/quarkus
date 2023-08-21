package org.acme.authenticationService.services;

import com.acme.authorization.security.Authorizer;
import com.acme.authorization.security.UserPrincipal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SelfAuthorizer implements Authorizer {

    @Inject
    JWTParser parser;
    @Inject
    ObjectMapper objectMapper;

    @Override
    public UserPrincipal authorize(String accessToken) {
        try {
            return TokenUtils.verifyAccessToken(accessToken, parser, objectMapper);
        } catch (ParseException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
