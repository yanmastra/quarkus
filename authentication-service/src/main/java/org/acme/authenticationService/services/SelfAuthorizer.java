package org.acme.authenticationService.services;

import com.acme.authorization.security.Authorizer;
import com.acme.authorization.security.UserPrincipal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.vertx.ext.web.handler.HttpException;
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
            throw new HttpException(HttpResponseStatus.BAD_REQUEST.code(), "Invalid Token");
        } catch (Exception ex) {
            throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), ex.getMessage());
        }
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
