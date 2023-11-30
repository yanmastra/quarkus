package org.acme.authenticationService.data.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.authenticationService.data.entity.UserRole;

@ApplicationScoped
public class UserRoleRepository implements PanacheRepositoryBase<UserRole, String> {
}
