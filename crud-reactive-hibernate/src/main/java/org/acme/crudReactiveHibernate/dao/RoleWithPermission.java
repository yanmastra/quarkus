package org.acme.crudReactiveHibernate.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.acme.crudReactiveHibernate.data.entity.RoleId;
import org.acme.crudReactiveHibernate.data.entity.RolePermission;

import java.util.ArrayList;
import java.util.List;

public class RoleWithPermission extends Role {

    @JsonProperty("permission")
    private List<Permission> permissions = new ArrayList<>();

    public RoleWithPermission(String code, String appCode, String name, String description) {
        super(code, appCode, name, description);
    }

    @Override
    public org.acme.crudReactiveHibernate.data.entity.Role toDto() {
        org.acme.crudReactiveHibernate.data.entity.Role role = new org.acme.crudReactiveHibernate.data.entity.Role(new RoleId(super.getAppCode(), super.getCode()), super.getName(), super.getDescription());
        for (Permission permission : permissions) {
            role.addPermission(permission.toDTO());
        }
        return role;
    }

    public static RoleWithPermission fromDTO(org.acme.crudReactiveHibernate.data.entity.Role dto) {
        RoleWithPermission role = new RoleWithPermission(dto.getId().getCode(), dto.getId().getAppCode(), dto.getName(), dto.getDescription());
        for (RolePermission rp : dto.getPermissions()) {
            role.addPermission(Permission.fromDTO(rp.getPermission()));
        }
        return role;
    }

    public void addPermission(Permission permission) {
        if (permissions == null) permissions = new ArrayList<>();
        if (!permissions.contains(permission))
            permissions.remove(permission);
        permissions.add(permission);
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }
}
