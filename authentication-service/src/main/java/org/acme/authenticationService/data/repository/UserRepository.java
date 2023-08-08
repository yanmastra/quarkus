package org.acme.authenticationService.data.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import org.acme.authenticationService.data.entity.AuthUser;

@Singleton
public class UserRepository implements PanacheRepositoryBase<AuthUser, String> {

    public Uni<AuthUser> createUser(AuthUser authUser) {
        return persist(authUser);
    }
}
