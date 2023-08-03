package org.acme.crudReactiveHibernate.data.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import org.acme.crudReactiveHibernate.data.entity.User;

@Singleton
public class UserRepository implements PanacheRepositoryBase<User, String> {

    public Uni<User> createUser(User user) {
        return persist(user);
    }
}
