package com.acme.authorization.security;

import org.acme.authorization.json.UserOnly;
import com.acme.authorization.utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.runtime.util.StringUtil;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.util.KeyUtils;
import io.vertx.ext.web.handler.HttpException;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;

public abstract class BaseAuthorizer implements Authorizer {
    @Inject
    ObjectMapper objectMapper;
    @Inject
    JWTParser jwtParser;
    @ConfigProperty(name = "security.application.secret-key", defaultValue = "")
    String secretKey;
    @ConfigProperty(name = "security.application.code", defaultValue = "")
    String applicationCode;
    @ConfigProperty(name = "security.application.public-key-location", defaultValue = "publickey.pub")
    String publicKey;

    @Override
    public UserPrincipal authorize(String accessToken) {
        try {
            return verifyToken(accessToken);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private UserPrincipal verifyToken(String accessToken) throws IOException, ParseException, GeneralSecurityException {
        if (StringUtil.isNullOrEmpty(accessToken)) throw new HttpException(HttpResponseStatus.FORBIDDEN.code(), "Token is null");
        JsonWebToken jsonWebToken1 = this.jwtParser.verify(accessToken, KeyUtils.readPublicKey(publicKey));

        String appCode = jsonWebToken1.getClaim(Constants.KEY_CLIENT_ID);
        if (!applicationCode.equals(appCode))
            throw new IllegalArgumentException("Invalid application code!");

        JsonWebToken jsonWebToken = this.jwtParser.decrypt(jsonWebToken1.getSubject(), this.secretKey);
        if (Instant.ofEpochSecond(jsonWebToken.getExpirationTime()).isBefore(Instant.now()))
            throw new HttpException(401, "Token expired");

        String subject = jsonWebToken.getSubject();
        UserOnly data = objectMapper.readValue(subject, UserOnly.class);

        return new UserPrincipal(data, jsonWebToken.getGroups().stream().toList(), appCode, accessToken);
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
