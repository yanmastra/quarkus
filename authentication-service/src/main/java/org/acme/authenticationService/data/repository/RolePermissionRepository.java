package org.acme.authenticationService.data.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.inject.Singleton;
import org.acme.authenticationService.data.entity.RolePermission;
import org.acme.authenticationService.data.entity.RolePermissionId;

@Singleton
public class RolePermissionRepository implements PanacheRepositoryBase<RolePermission, RolePermissionId> {
}
