package org.acme.data;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;

@Singleton
public class UserRepository implements PanacheRepository<User> {

    public Uni<User> createUser(User user) {
        return persist(user);
    }
}
