package org.acme.authenticationService.services;

import com.acme.authorization.json.AuthenticationResponse;
import com.acme.authorization.json.SignInCredential;
import com.acme.authorization.security.UserPrincipal;
import com.acme.authorization.utils.DateTimeUtils;
import com.acme.authorization.utils.PasswordGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Parameters;
import io.quarkus.runtime.util.StringUtil;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.handler.HttpException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.authenticationService.dao.UserOnly;
import org.acme.authenticationService.data.entity.RolePermission;
import org.acme.authenticationService.data.entity.UserRole;
import org.acme.authenticationService.data.repository.ApplicationRepository;
import org.acme.authenticationService.data.repository.UserRepository;
import org.acme.authenticationService.data.repository.UserRoleRepository;
import org.jboss.logging.Logger;

import java.util.*;

@ApplicationScoped
public class AuthenticationService {

    @Inject
    UserRepository userRepository;
    @Inject
    ApplicationRepository appRepository;
    @Inject
    UserRoleRepository userRoleRepository;
    @Inject
    ObjectMapper objectMapper;
    @Inject
    JWTParser parser;
    @Inject
    Logger logger;

    static final String SESSION = "SESSION";
    static final String APPLICATION = "APPLICATION";

    @WithTransaction
    public Uni<AuthenticationResponse> authenticate(SignInCredential credential) {
        return userRepository.find("(username=?1 or email = ?1) and verified", credential.username)
                .filter("isActiveUser", Parameters.with("isActive", true))
                .firstResult().chain(user -> {
                    if (user == null) {
                        throw new HttpException(HttpResponseStatus.NOT_FOUND.code(), "Invalid credential");
                    }

                    return appRepository.findById(credential.appCode).chain(app -> {
                        if (app == null) throw new HttpException(HttpResponseStatus.FORBIDDEN.code(), "Application not found");

                        if (user.validatePassword(credential.password)) {
                            return userRoleRepository.find("authUser=?1 and role.appCode=?2", user, credential.appCode).firstResult().chain(userRole -> {
                                if (userRole == null) {
                                    logger.error("Role with user:"+user.getId()+", appCode:"+credential.appCode+" not found");
                                    throw new HttpException(HttpResponseStatus.FORBIDDEN.code(), "Access to the application is denied!");
                                }

                                user.setRoles(new HashSet<>(Collections.singletonList(userRole)));
                                UserOnly userData = UserOnly.fromDto(user);
                                Set<String> roleGroup = new HashSet<>();
                                String tokenId = UUID.randomUUID().toString();
                                String secretKey = app.getSecretKey();
                                String refreshKey = PasswordGenerator.generatePassword(32, true);

                                for (UserRole ur: user.getRoles()) {
                                    roleGroup.add(ur.getRole().getCode());
                                    if (!ur.getRole().getCode().equalsIgnoreCase("SERVICE"))
                                        roleGroup.add("USER");
                                    for (RolePermission rp: ur.getRole().getPermissions()) roleGroup.add(rp.getPermission().getCode());
                                }

                                try {
                                    String subject = objectMapper.writeValueAsString(userData);

                                    Date accessExpired = credential.expToken == null ? DateTimeUtils.getExpiredToken() : credential.expToken;
                                    Date refreshExpired = DateTimeUtils.getExpiredRefreshToken();
                                    String accessToken = TokenUtils.createAccessToken(app.getCode(), subject, userData.getUsername(), tokenId, accessExpired, roleGroup, secretKey);
                                    String refreshToken = TokenUtils.createRefreshToken(app.getCode(), subject, userData.getUsername(), tokenId, refreshExpired, roleGroup, refreshKey);

                                    AuthenticationResponse authResponse = new AuthenticationResponse(accessToken, refreshToken, userData);

                                    TokenUtils.saveSession(tokenId, app.getCode(), secretKey, refreshKey);
                                    return Uni.createFrom().item(authResponse);
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        } else {
                            throw new HttpException(HttpResponseStatus.NOT_FOUND.code(), "Invalid credential");
                        }
                    });
                });
    }

    public Uni<UserPrincipal> authorizeToken(String auth) {
        if (!StringUtil.isNullOrEmpty(auth)) {
            if (auth.startsWith("Bearer ")) auth = auth.replace("Bearer ", "");
        } else {
            throw new HttpException(401, "Unauthorized");
        }

        return Uni.createFrom().item(auth).chain(token -> {
            try {
                return Uni.createFrom().item(TokenUtils.verifyAccessToken(token, parser, objectMapper));
            } catch (ParseException | JsonProcessingException e) {
                logger.error(e.getMessage(), e);
                throw new HttpException(HttpResponseStatus.FORBIDDEN.code(), e.getMessage(), e);
            }
        });
    }


    public Uni<AuthenticationResponse> refreshToken(String auth) {
        return Uni.createFrom().item(auth).chain(token -> {
            try {
                return Uni.createFrom().item(TokenUtils.createAccessToken(token, parser, objectMapper));
            } catch (ParseException | JsonProcessingException e) {
                throw new HttpException(HttpResponseStatus.BAD_REQUEST.code(), "Invalid Token");
            } catch (Throwable e) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), e.getMessage());
            }
        });
    }
}
