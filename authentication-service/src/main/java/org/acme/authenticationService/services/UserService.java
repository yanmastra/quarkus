package org.acme.authenticationService.services;

import com.acme.authorization.security.UserPrincipal;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.acme.authenticationService.dao.User;
import org.acme.authenticationService.dao.UserOnly;
import org.acme.authenticationService.dao.UserWithPermission;
import org.acme.authenticationService.data.repository.UserRepository;
import org.acme.authenticationService.data.repository.UserRoleRepository;
import org.jboss.logging.Logger;

import java.util.HashSet;
import java.util.List;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;
    @Inject
    UserRoleRepository userRoleRepository;

    @Inject
    Logger logger;

    @WithTransaction
    public Uni<User> saveUser(UserOnly user) {
        return userRepository.createUser(user.toDto()).onItem().transform(UserOnly::fromDto);
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
}
