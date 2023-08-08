package org.acme.authenticationService.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.acme.authenticationService.data.entity.RolePermission;

import java.util.ArrayList;
import java.util.List;

public class RoleWithPermission extends Role {

    @JsonProperty("permission")
    private List<Permission> permissions = new ArrayList<>();

    public RoleWithPermission(String id, String code, String appCode, String name, String description) {
        super(id, code, appCode, name, description);
    }

    @Override
    public org.acme.authenticationService.data.entity.Role toDto() {
        org.acme.authenticationService.data.entity.Role role = new org.acme.authenticationService.data.entity.Role(super.getId(), super.getAppCode(), super.getCode(), super.getName(), super.getDescription());
        for (Permission permission : permissions) {
            role.addPermission(permission.toDTO());
        }
        return role;
    }

    public static RoleWithPermission fromDTO(org.acme.authenticationService.data.entity.Role dto) {
        RoleWithPermission role = new RoleWithPermission(dto.getId(), dto.getCode(), dto.getAppCode(), dto.getName(), dto.getDescription());
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
