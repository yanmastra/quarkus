package org.acme.authenticationService.services;

import com.acme.authorization.security.UserPrincipal;
import com.acme.authorization.utils.PasswordGenerator;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.acme.authenticationService.dao.User;
import org.acme.authenticationService.dao.UserOnly;
import org.acme.authenticationService.dao.UserWithPermission;
import org.acme.authenticationService.data.entity.AuthUser;
import org.acme.authenticationService.data.entity.UserApp;
import org.acme.authenticationService.data.entity.UserRole;
import org.acme.authenticationService.data.repository.*;
import org.jboss.logging.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;
    @Inject
    UserRoleRepository userRoleRepository;
    @Inject
    ApplicationRepository appRepo;
    @Inject
    RoleRepository roleRepo;
    @Inject MailService mailService;
    @Inject
    UserAppRepository userAppRepository;

    @Inject
    Logger logger;

    @WithTransaction
    public Uni<User> saveUser(UserOnly user) {
        return userRepository.persist(user.toDto()).onItem().transform(UserOnly::fromDto);
    }

    @WithTransaction
    public Uni<UserOnly> updateUser(String id, UserOnly user) {
        return userRepository.update("name = ?1 where id = ?2", user.getName(), id)
                .onItem().transformToUni(integer -> {
                    logger.info("updated result: " + integer + ", id:" + id);
                    return userRepository.find("id = ?1", id).firstResult().onItem().transform(UserOnly::fromDto);
                })
                .onFailure().invoke(throwable -> logger.error(throwable.getMessage(), throwable));

    }

    @WithTransaction
    public Uni<List<UserOnly>> findAll() {
        return userRepository.findAll(Sort.ascending("email")).list()
                .map(obj -> obj.stream().map(UserOnly::fromDto).toList());
    }

    @WithTransaction
    public Uni<UserWithPermission> findDetail(String id) {
        return userRepository.find("id = ?1", id).firstResult().chain(user -> {
            if (user != null) return userRoleRepository.find("authUser=?1", user).list().map(result -> {
                user.setRoles(new HashSet<>(result));
                return UserWithPermission.fromDto(user);
            });
            else throw new NotFoundException("AuthUser not found");
        });
    }

    public Uni<?> createNewUser(AuthUser authUser, String appCode, String roleCode, UserPrincipal principal) {

        String password = PasswordGenerator.generatePassword(12, true);
        authUser.setPasswordTextPlain(password);
        authUser.setCreatedBy(principal.getName());

        return appRepo.findById(appCode)
                .chain(app -> roleRepo.findById(app.getCode(), roleCode).chain(role -> {
                    authUser.setVerified(true);

                    return userRepository.persist(authUser)
                            .chain(saved -> {
                                logger.info("saved user:"+saved+", role:"+role);
                                return userRoleRepository.persist(new UserRole(saved, role));
                            })
                            .chain(saved -> {
                                logger.info("saved userRole:"+saved+", app:"+app);
                                return userAppRepository.persist(new UserApp(saved.getUser(), app));
                            })
                            .map(r -> Map.of("appName", app.getName(), "roleName", role.getName()));

                }))
                .onItem().call(r -> {
                    logger.info("sending email to:"+authUser.getEmail());
                    return mailService.createSignInfoEmail(
                                    "EN",
                                    authUser.getUsername(),
                                    password,
                                    authUser.getName(),
                                    authUser.getEmail(),
                                    r.get("appName"),
                                    r.get("roleName"))
                            .onFailure().invoke(throwable -> logger.error("error sending email:"+throwable.getMessage(), throwable));
                });
    }
}
