package org.acme.authorization.json;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class RoleWithPermission extends Role {

    @JsonProperty("permission")
    private List<Permission> permissions = new ArrayList<>();

    public RoleWithPermission(String id, String code, String appCode, String name, String description) {
        super(id, code, appCode, name, description);
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
