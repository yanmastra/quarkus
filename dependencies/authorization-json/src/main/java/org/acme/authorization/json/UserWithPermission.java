package org.acme.authorization.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserWithPermission extends User {

    public UserWithPermission() {
    }

    @JsonProperty("roles")
    private List<RoleWithPermission> roles;

    public UserWithPermission(String id, String username, String email, String name) {
        super(id, username, email, name);
    }

    public List<RoleWithPermission> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleWithPermission> roles) {
        this.roles = roles;
    }
}
