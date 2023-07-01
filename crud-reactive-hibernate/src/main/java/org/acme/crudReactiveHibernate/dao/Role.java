package org.acme.crudReactiveHibernate.dao;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.acme.crudReactiveHibernate.data.entity.RoleId;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Role {
    @JsonProperty("app_code")
    private String appCode;
    @JsonProperty("code")
    private String code;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;

    @JsonProperty("permission")
    private List<Permission> permissions;

    public Role() {
    }

    public Role(String code, String appCode, String name, String description) {
        this.code = code;
        this.appCode = appCode;
        this.name = name;
        this.description = description;
    }

    public static Role fromDTO(org.acme.crudReactiveHibernate.data.entity.Role dto) {
        return new Role(dto.getId().getCode(), dto.getId().getAppCode(), dto.getName(), dto.getDescription());
    }

    public org.acme.crudReactiveHibernate.data.entity.Role toDTO() {
        org.acme.crudReactiveHibernate.data.entity.Role dRole = new org.acme.crudReactiveHibernate.data.entity.Role(new RoleId(appCode, code), name, description);
        if (this.permissions != null)
            for (Permission permission: this.permissions) {
                dRole.addPermission(permission.toDTO());
            }
        return dRole;
    }

    public void addPermission(Permission permission) {
        if (permissions == null) permissions = new ArrayList<>();
        if (!permissions.contains(permission))
            permissions.remove(permission);
        permissions.add(permission);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }
}
