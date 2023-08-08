package org.acme.authenticationService.dao;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.acme.authenticationService.data.entity.AuthUser;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserWithPermission extends User implements Principal {

    public UserWithPermission() {
    }

    @JsonProperty("roles")
    private List<RoleWithPermission> roles;

    public UserWithPermission clearTimestamp() {
        setDeletedAt(null);
        setDeletedBy(null);
        setUpdatedAt(null);
        setUpdatedBy(null);
        setCreatedAt(null);
        setCreatedBy(null);
        return this;
    }

    @Override
    public AuthUser toDto() {
        AuthUser authUser = new AuthUser(getId(), getUsername(), getEmail(), getName());
        if (roles != null) {
            for (RoleWithPermission role : roles) {
                authUser.addRole(role.toDto());
            }
        }
        return authUser;
    }

    public UserWithPermission(String id, String username, String email, String name) {
        super(id, username, email, name);
    }

    public static UserWithPermission fromDto(AuthUser authUser) {
        UserWithPermission nUser = new UserWithPermission(authUser.getId(), authUser.getUsername(), authUser.getEmail(), authUser.getName());
        if (authUser.getRoles() != null) {
            nUser.roles = new ArrayList<>();
            for (org.acme.authenticationService.data.entity.UserRole ur : authUser.getRoles()) {
                nUser.roles.add(RoleWithPermission.fromDTO(ur.getRole()));
            }
        }
        nUser.setCreatedAt(authUser.getCreatedAt());
        nUser.setCreatedBy(authUser.getCreatedBy());
        nUser.setUpdatedAt(authUser.getUpdatedAt());
        nUser.setUpdatedBy(authUser.getUpdatedBy());
        nUser.setDeletedAt(authUser.getDeletedAt());
        nUser.setDeletedBy(authUser.getDeletedBy());
        return nUser;
    }

    public List<RoleWithPermission> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleWithPermission> roles) {
        this.roles = roles;
    }
}
