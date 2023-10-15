package org.acme.authenticationService.services;

import com.acme.authorization.json.AuthenticationResponse;
import com.acme.authorization.json.UserOnly;
import com.acme.authorization.security.UserPrincipal;
import com.acme.authorization.utils.CacheUpdateMode;
import com.acme.authorization.utils.DateTimeUtils;
import com.acme.authorization.utils.KeyValueCacheUtils;
import com.acme.authorization.utils.PasswordGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.runtime.util.StringUtil;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import io.vertx.ext.web.handler.HttpException;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Date;
import java.util.Set;

public class TokenUtils {
    static String createAccessToken(String appCode, String subject, String username, String tokenId, Date expiredAt, Set<String> permissions, String secretKey) {
        String token = Jwt.subject(subject)
                .claim(Claims.preferred_username, username)
                .expiresAt(expiredAt.getTime())
                .issuer(tokenId)
                .groups(permissions)
                .audience("access_token")
                .jwe().encryptWithSecret(secretKey);
        return token + '.'+appCode;
    }

    static String createRefreshToken(String appCode, String subject, String username, String tokenId, Date expiredAt, Set<String> permissions, String secretKey) {
        String token = Jwt.subject(subject)
                .claim(Claims.preferred_username, username)
                .expiresAt(expiredAt.getTime())
                .issuer(appCode)
                .groups(permissions)
                .audience("refresh_token")
                .jwe().encryptWithSecret(secretKey);
        return token + '.'+tokenId;
    }

    static UserPrincipal verifyAccessToken(String accessToken, JWTParser parser, ObjectMapper objectMapper) throws ParseException, JsonProcessingException {
        if (StringUtil.isNullOrEmpty(accessToken)) throw new HttpException(HttpResponseStatus.FORBIDDEN.code(), "Token is null");

        String appCode = accessToken.substring(accessToken.lastIndexOf('.')+1);
        String auth = accessToken.replaceAll('.'+appCode, "");
        String secretKey = findAppSecret(appCode);
        JsonWebToken jsonWebToken = parser.decrypt(auth, secretKey);
        if ((new Date()).after(new Date(jsonWebToken.getExpirationTime())))
            throw new HttpException(401, "Token expired");

        String subject = jsonWebToken.getSubject();
        UserOnly data = objectMapper.readValue(subject, UserOnly.class);

        return new UserPrincipal(data, jsonWebToken.getGroups().stream().toList(), appCode, accessToken);
    }

    static AuthenticationResponse createAccessToken(String refreshToken, JWTParser parser, ObjectMapper objectMapper) throws ParseException, JsonProcessingException {
        if (StringUtil.isNullOrEmpty(refreshToken)) throw new HttpException(HttpResponseStatus.FORBIDDEN.code(), "Token is null");

        String tokenId = refreshToken.substring(refreshToken.lastIndexOf('.')+1);
        String refreshKey = findRefreshKey(tokenId);
        String appCode = findAppCode(tokenId);
        String auth = refreshToken.replaceAll('.'+tokenId, "");

        JsonWebToken jsonWebToken = parser.decrypt(auth, refreshKey);
        if ((new Date()).after(new Date(jsonWebToken.getExpirationTime()))){
            throw new HttpException(HttpResponseStatus.UNAUTHORIZED.code(), "Token expired");
        }

        String subject = jsonWebToken.getSubject();
        Set<String> allowedPermissions = jsonWebToken.getGroups();
        String username = (String) jsonWebToken.claim(Claims.preferred_username).orElse(null);

        String appSecretKey = findAppSecret(appCode);
        String newRefreshKey = PasswordGenerator.generatePassword(32, true);


        String accessToken = createAccessToken(appCode, subject, username, tokenId, DateTimeUtils.getExpiredToken(), allowedPermissions, appSecretKey);
        String newRefreshToken = createRefreshToken(appCode, subject, username, tokenId, DateTimeUtils.getExpiredRefreshToken(), allowedPermissions, newRefreshKey);
        UserOnly userOnly = objectMapper.readValue(subject, UserOnly.class);

        saveSession(tokenId, appCode, appSecretKey, newRefreshKey);

        return new AuthenticationResponse(accessToken, newRefreshToken, userOnly);
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
        KeyValueCacheUtils.saveCache(AuthenticationService.APPLICATION, appCode+"_SEC", appSecretKey, CacheUpdateMode.ADD);
    }
}
