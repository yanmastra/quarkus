package org.acme.authenticationService.services;

import com.acme.authorization.json.AuthenticationResponse;
import org.acme.authorization.json.UserOnly;
import com.acme.authorization.security.UserPrincipal;
import com.acme.authorization.utils.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.runtime.util.StringUtil;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import io.smallrye.jwt.util.KeyUtils;
import io.vertx.ext.web.handler.HttpException;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

public class TokenUtils {
    private static final Logger log = Logger.getLogger(TokenUtils.class);

    static String createAccessToken(String appCode, String subject, Date expiredAt, Set<String> permissions, String secretKey){
        String hostPort = "http://localhost:" + ConfigProvider.getConfig().getValue("quarkus.http.port", String.class)+"/api/v1/auth";
        return createAccessToken(hostPort, appCode, subject, expiredAt, permissions, secretKey);
    }

    static String createAccessToken(String issuer, String appCode, String subject, Date expiredAt, Set<String> permissions, String secretKey){
        return createAccessToken(issuer, appCode, subject, null, expiredAt, permissions, secretKey);
    }

    static String createAccessToken(String issuer, String appCode, String subject, String firebaseToken, Date expiredAt, Set<String> permissions, String secretKey) {
        long expired = Instant.ofEpochMilli(expiredAt.getTime()).getEpochSecond();
        long issuedAt = Instant.now().getEpochSecond();

        JwtClaimsBuilder jcb = Jwt.subject(subject)
                .expiresAt(expired)
                .groups(permissions)
                .audience("access_token");

        if (StringUtils.isNotBlank(firebaseToken)) {
                jcb.claim(Constants.FIREBASE_TOKEN, firebaseToken);
        }
        String jwe = jcb.jwe().encryptWithSecret(secretKey);
        return Jwt.subject(jwe)
                .expiresAt(expired)
                .claim(Constants.KEY_CLIENT_ID, appCode)
                .issuer(issuer)
                .issuedAt(issuedAt)
                .sign();
    }

    static String createRefreshToken(String subject, String tokenId, Date expiredAt, Set<String> permissions, String secretKey) {
        String hostPort = "http://localhost:" + ConfigProvider.getConfig().getValue("quarkus.http.port", String.class)+"/api/v1/auth";
        return createRefreshToken(hostPort, subject, tokenId, expiredAt, permissions, secretKey);
    }

    static String createRefreshToken(String issuer, String subject, String tokenId, Date expiredAt, Set<String> permissions, String secretKey) {
        return createRefreshToken(issuer, subject, null, tokenId, expiredAt, permissions, secretKey);
    }

    static String createRefreshToken(String issuer, String subject, String firebaseRefreshToken, String tokenId, Date expiredAt, Set<String> permissions, String secretKey) {
        long expired = Instant.ofEpochMilli(expiredAt.getTime()).getEpochSecond();
        long issuedAt = Instant.now().getEpochSecond();

        JwtClaimsBuilder jcb = Jwt.subject(subject)
                .expiresAt(expired)
                .groups(permissions)
                .audience("refresh_token");

        if (StringUtils.isNotBlank(firebaseRefreshToken)) {
            jcb.claim(Constants.FIREBASE_REFRESH_TOKEN, firebaseRefreshToken);
        }

        String jwe = jcb.jwe().encryptWithSecret(secretKey);
        return Jwt.subject(jwe)
                .expiresAt(expired)
                .claim(Constants.KEY_CLIENT_ID, tokenId)
                .issuer(issuer)
                .issuedAt(issuedAt)
                .sign();
    }

    static UserPrincipal verifyAccessToken(String accessToken, JWTParser parser, ObjectMapper objectMapper) throws ParseException, IOException, GeneralSecurityException {
        if (StringUtil.isNullOrEmpty(accessToken)) throw new HttpException(HttpResponseStatus.FORBIDDEN.code(), "Token is null");
        JsonWebToken jsonWebToken1 = parser.verify(accessToken, KeyUtils.readPublicKey("publickey.pub"));

        String appCode = jsonWebToken1.getClaim(Constants.KEY_CLIENT_ID);
        String secretKey = findAppSecret(appCode);
        JsonWebToken jsonWebToken = parser.decrypt(jsonWebToken1.getSubject(), secretKey);
        if (Instant.ofEpochSecond(jsonWebToken.getExpirationTime()).isBefore(Instant.now()))
            throw new HttpException(401, "Token expired");

        String subject = jsonWebToken.getSubject();
        UserOnly data = objectMapper.readValue(subject, UserOnly.class);
        Optional<String> firebaseToken = jsonWebToken.claim(Constants.FIREBASE_TOKEN);

        return firebaseToken.map(s -> new UserPrincipal(data, jsonWebToken.getGroups().stream().toList(), appCode, accessToken, s))
                .orElseGet(() -> new UserPrincipal(data, jsonWebToken.getGroups().stream().toList(), appCode, accessToken));
    }

    static AuthenticationResponse<org.acme.authenticationService.dao.UserOnly> createAccessToken(String issuer, String refreshToken, JWTParser parser, ObjectMapper objectMapper) throws ParseException, IOException, GeneralSecurityException {
        if (StringUtil.isNullOrEmpty(refreshToken)) throw new HttpException(HttpResponseStatus.FORBIDDEN.code(), "Token is null");

        JsonWebToken jwt1 = parser.verify(refreshToken, KeyUtils.readPublicKey("publickey.pub"));


        String tokenId = jwt1.getClaim(Constants.KEY_CLIENT_ID); //refreshToken.substring(refreshToken.lastIndexOf('.')+1);
        String refreshKey = findRefreshKey(tokenId);
        String appCode = findAppCode(tokenId);
//        String auth = refreshToken.replaceAll('.'+tokenId, "");

        JsonWebToken jsonWebToken = parser.decrypt(jwt1.getSubject(), refreshKey);
        if ((new Date()).after(new Date(jsonWebToken.getExpirationTime()))){
            throw new HttpException(HttpResponseStatus.UNAUTHORIZED.code(), "Token expired");
        }

        String subject = jsonWebToken.getSubject();
        Set<String> allowedPermissions = jsonWebToken.getGroups();
        String username = (String) jsonWebToken.claim(Claims.preferred_username).orElse(null);

        String appSecretKey = findAppSecret(appCode);
        String newRefreshSecretKey = PasswordGenerator.generatePassword(32, true);


        String accessToken = createAccessToken(issuer, appCode, subject, DateTimeUtils.getExpiredToken(), allowedPermissions, appSecretKey);
        String newRefreshToken = createRefreshToken(issuer, subject, tokenId, DateTimeUtils.getExpiredRefreshToken(), allowedPermissions, newRefreshSecretKey);
        org.acme.authenticationService.dao.UserOnly userOnly = objectMapper.readValue(subject, org.acme.authenticationService.dao.UserOnly.class);

        saveSession(tokenId, appCode, appSecretKey, newRefreshSecretKey);

        return new AuthenticationResponse<>(accessToken, newRefreshToken, userOnly);
    }

    private static String findAppSecret(String appCode) {
        return KeyValueCacheUtils.findCache(AuthenticationService.APPLICATION, appCode+"_SEC");
    }

    static String findAppCode(String tokenId) {
        return KeyValueCacheUtils.findCache(tokenId, AuthenticationService.SESSION+"_APP");
    }

    static String findRefreshKey(String tokenId) {
        return KeyValueCacheUtils.findCache(tokenId, AuthenticationService.SESSION+"_REF");
    }

    static void saveSession(String tokenId, String appCode, String appSecretKey, String refreshKey) {
        KeyValueCacheUtils.saveCache(tokenId, AuthenticationService.SESSION+"_APP", appCode, CacheUpdateMode.ADD);
        KeyValueCacheUtils.saveCache(tokenId, AuthenticationService.SESSION+"_REF", refreshKey, CacheUpdateMode.ADD);
        KeyValueCacheUtils.saveCache(AuthenticationService.APPLICATION, appCode+"_SEC", appSecretKey, CacheUpdateMode.REPLACE);
    }

    static String getIssuer(ContainerRequestContext context) {
        return context.getUriInfo().getRequestUri().toString();
    }
}
