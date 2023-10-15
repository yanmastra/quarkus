package org.acme.authenticationService.data.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.authenticationService.data.entity.AuthUser;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<AuthUser, String> {

    public Uni<AuthUser> createUser(AuthUser authUser) {
        return persist(authUser);
    }
}
