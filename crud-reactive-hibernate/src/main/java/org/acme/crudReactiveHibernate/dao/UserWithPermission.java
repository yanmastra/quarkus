package org.acme.crudReactiveHibernate.dao;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserWithPermission extends User {

    public UserWithPermission() {
    }

    @JsonProperty("roles")
    private List<RoleWithPermission> roles;

    @Override
    public org.acme.crudReactiveHibernate.data.entity.User toDto() {
        org.acme.crudReactiveHibernate.data.entity.User user = new org.acme.crudReactiveHibernate.data.entity.User(getId(), getUsername(), getEmail(), getName());
        if (roles != null) {
            for (RoleWithPermission role : roles) {
                user.addRole(role.toDto());
            }
        }
        return user;
    }

    public UserWithPermission(String id, String username, String email, String name) {
        super(id, username, email, name);
    }

    public static UserWithPermission fromDto(org.acme.crudReactiveHibernate.data.entity.User user) {
        UserWithPermission nUser = new UserWithPermission(user.getId(), user.getUsername(), user.getEmail(), user.getName());
        if (user.getRoles() != null) {
            nUser.roles = new ArrayList<>();
            for (org.acme.crudReactiveHibernate.data.entity.UserRole ur : user.getRoles()) {
                nUser.roles.add(RoleWithPermission.fromDTO(ur.getRole()));
            }
        }
        nUser.setCreatedAt(user.getCreatedAt());
        nUser.setCreatedBy(user.getCreatedBy());
        nUser.setUpdatedAt(user.getUpdatedAt());
        nUser.setUpdatedBy(user.getUpdatedBy());
        nUser.setDeletedAt(user.getDeletedAt());
        nUser.setDeletedBy(user.getDeletedBy());
        return nUser;
    }

    public List<RoleWithPermission> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleWithPermission> roles) {
        this.roles = roles;
    }
}
