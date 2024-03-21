package org.acme.authenticationService.services;

import org.acme.authorization.json.AuthenticationResponse;
import org.acme.authorization.json.Credential;
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
import jakarta.ws.rs.container.ContainerRequestContext;
import org.acme.authenticationService.dao.UserOnly;
import org.acme.authenticationService.data.entity.RolePermission;
import org.acme.authenticationService.data.entity.UserRole;
import org.acme.authenticationService.data.repository.ApplicationRepository;
import org.acme.authenticationService.data.repository.UserRepository;
import org.acme.authenticationService.data.repository.UserRoleRepository;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.security.GeneralSecurityException;
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
    public Uni<AuthenticationResponse<UserOnly>> authenticate(Credential credential, ContainerRequestContext context) {
        return userRepository.find("(username=?1 or email = ?1) and verified", credential.getUsername())
                .filter("isActiveUser", Parameters.with("isActive", true))
                .firstResult().chain(user -> {
                    if (user == null) {
                        throw new HttpException(HttpResponseStatus.NOT_FOUND.code(), "Invalid credential");
                    }

                    return appRepository.findById(credential.getAppCode()).chain(app -> {
                        if (app == null) throw new HttpException(HttpResponseStatus.FORBIDDEN.code(), "Application not found");

                        if (user.validatePassword(credential.getPassword())) {
                            return userRoleRepository.find("authUser.id=?1 and role.appCode=?2", user.getId(), credential.getAppCode()).firstResult().chain(userRole -> {
                                if (userRole == null) {
                                    logger.error("Role with user:"+user.getId()+", appCode:"+credential.getAppCode()+" not found");
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

                                    Date accessExpired = credential.getExpToken() == null ? DateTimeUtils.getExpiredToken() : credential.getExpToken();
                                    Date refreshExpired = DateTimeUtils.getExpiredRefreshToken();

                                    String issuer = TokenUtils.getIssuer(context);
                                    String accessToken = TokenUtils.createAccessToken(issuer, app.getCode(), subject, accessExpired, roleGroup, secretKey);
                                    String refreshToken = TokenUtils.createRefreshToken(issuer, subject, tokenId, refreshExpired, roleGroup, refreshKey);

                                    AuthenticationResponse<UserOnly> authResponse = new AuthenticationResponse<>(accessToken, refreshToken, userData);

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
            } catch (GeneralSecurityException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


    public Uni<AuthenticationResponse<UserOnly>> refreshToken(String auth, ContainerRequestContext context) {
        return Uni.createFrom().item(auth).chain(token -> {
            try {
                String issuer = TokenUtils.getIssuer(context);
                return Uni.createFrom().item(TokenUtils.createAccessToken(issuer, token, parser, objectMapper));
            } catch (ParseException | JsonProcessingException e) {
                throw new HttpException(HttpResponseStatus.BAD_REQUEST.code(), "Invalid Token");
            } catch (Throwable e) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), e.getMessage());
            }
        });
    }
}
