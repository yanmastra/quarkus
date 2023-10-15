package org.acme.authenticationService.data.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.authenticationService.data.entity.RolePermission;
import org.acme.authenticationService.data.entity.RolePermissionId;

@ApplicationScoped
public class RolePermissionRepository implements PanacheRepositoryBase<RolePermission, RolePermissionId> {
}
