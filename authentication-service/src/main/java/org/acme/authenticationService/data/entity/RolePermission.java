package org.acme.authenticationService.data.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "role_permission", indexes = {
        @Index(name = "FK54dlsmecjg5cjcuyxjspj6coo", columnList = "permission_id")
})
public class RolePermission extends PanacheEntityBase {
    @EmbeddedId
    private final RolePermissionId id;

    public RolePermission() {
        id = new RolePermissionId();
    }

    public RolePermission(Role role, Permission permission) {
        id = new RolePermissionId(role, permission);
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj instanceof RolePermission pObj) {
            return id.equals(pObj.id);
        }
        return false;
    }


    public Role getRole() {
        return id.getRole();
    }

    public void setRole(Role role) {
        this.id.setRole(role);
    }

    public Permission getPermission() {
        return id.getPermission();
    }

    public void setPermission(Permission permission) {
        this.id.setPermission(permission);
    }
}
