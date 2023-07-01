package org.acme.crudReactiveHibernate;

import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.crudReactiveHibernate.dao.User;
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
    public Uni<User> saveUser(User user) {
        return userRepository.createUser(user.toDto()).onItem().transform(User::fromDto);
    }

    @WithSession
    public Uni<List<User>> findAll() {
        return userRepository.findAll(Sort.ascending("email")).list()
                .map(obj -> obj.stream().map(User::fromDto).toList());
    }
}
