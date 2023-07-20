package org.acme.crudReactiveHibernate.data.entity;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Embeddable
public class RolePermissionId {
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumns({
            @JoinColumn(name = "app_code", referencedColumnName = "app_code", nullable = false),
            @JoinColumn(name = "role_code", referencedColumnName = "code", nullable = false)
    })
    private Role role;
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "permission_code", referencedColumnName = "id", nullable = false)
    private Permission permission;


    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (role == null || permission == null) return false;
        if (obj instanceof RolePermissionId pObj) {
            return role.equals(pObj.role) && permission.equals(pObj.permission);
        }
        return false;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        hcb.append(role);
        hcb.append(permission);
        return hcb.toHashCode();
    }

    public RolePermissionId() {
    }

    public RolePermissionId(Role role, Permission permission) {
        this.role = role;
        this.permission = permission;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }
}
