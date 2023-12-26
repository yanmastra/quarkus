package org.acme.authenticationService.data.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.authenticationService.data.entity.UserApp;

@ApplicationScoped
public class UserAppRepository implements PanacheRepositoryBase<UserApp, String> {
}
