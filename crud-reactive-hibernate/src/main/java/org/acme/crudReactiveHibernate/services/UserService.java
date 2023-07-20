package org.acme.crudReactiveHibernate.services;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.acme.crudReactiveHibernate.dao.User;
import org.acme.crudReactiveHibernate.dao.UserOnly;
import org.acme.crudReactiveHibernate.dao.UserWithPermission;
import org.acme.crudReactiveHibernate.data.UserRepository;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

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
        return userRepository.find("id = ?1", id).firstResult().map(user -> {
            if (user != null) return UserWithPermission.fromDto(user);
            else throw new NotFoundException("User not found");
        });
    }
}
