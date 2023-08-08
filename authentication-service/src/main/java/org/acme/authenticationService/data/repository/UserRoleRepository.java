package org.acme.authenticationService.data.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.inject.Singleton;
import org.acme.authenticationService.data.entity.UserRole;
import org.acme.authenticationService.data.entity.UserRoleId;

@Singleton
public class UserRoleRepository implements PanacheRepositoryBase<UserRole, UserRoleId> {
}
