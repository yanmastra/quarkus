package org.acme.authenticationService.services;

import com.acme.authorization.json.AuthenticationResponse;
import com.acme.authorization.json.SignInCredential;
import com.acme.authorization.utils.DateTimeUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import io.smallrye.jwt.build.Jwt;
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
import org.eclipse.microprofile.jwt.Claims;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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

    @WithTransaction
    public Uni<AuthenticationResponse> authenticate(SignInCredential credential) {
        return userRepository.find("username=?1 or email = ?1", credential.username)
                .filter("isActiveUser", Parameters.with("isActive", true))
                .firstResult().chain(user -> {
                    Log.info("result: "+user);
                    if (user == null) {
                        throw new HttpException(401, "Invalid credential");
                    }

                    return appRepository.findById(credential.appCode).chain(app -> {
                        if (user.validatePassword(credential.password)) {
                            return userRoleRepository.find("authUser=?1 and role.appCode=?2", user, credential.appCode).firstResult().chain(userRole -> {
                                if (userRole == null) {
                                    throw new HttpException(401, "Access to the application is denied!");
                                }

                                user.setRoles(new HashSet<>(Collections.singletonList(userRole)));
                                UserOnly userOnly = UserOnly.fromDto(user);
                                Set<String> roleGroup = new HashSet<>();

                                for (UserRole ur: user.getRoles()) {
                                    roleGroup.add(ur.getRole().getCode());
                                    for (RolePermission rp: ur.getRole().getPermissions()) roleGroup.add(rp.getPermission().getCode());
                                }

                                try {
                                    String subject = objectMapper.writeValueAsString(userOnly);

                                    Date accessExpired = DateTimeUtils.getExpiredToken();
                                    Date refreshExpired = DateTimeUtils.getExpiredRefreshToken();
                                    String accessToken = Jwt.subject(subject)
                                            .claim(Claims.preferred_username, userOnly.getUsername())
                                            .expiresAt(accessExpired.getTime())
                                            .issuer(credential.appCode)
                                            .audience("access_token")
                                            .groups(roleGroup)
                                            .jwe().encryptWithSecret(app.getSecretKey());

                                    String refreshToken = Jwt.subject(subject)
                                            .expiresAt(refreshExpired.getTime())
                                            .issuer(credential.appCode)
                                            .audience("refresh_token")
                                            .groups(roleGroup)
                                            .jwe().encryptWithSecret(app.getSecretKey());

                                    AuthenticationResponse authResponse = new AuthenticationResponse(accessToken, refreshToken, userOnly);
                                    return Uni.createFrom().item(authResponse);
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        } else {
                            throw new HttpException(401, "Invalid credential");
                        }
                    });
                });
    }
}
