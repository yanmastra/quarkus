package org.acme.crudReactiveHibernate.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.acme.crudReactiveHibernate.data.entity.UserRole;

import java.util.*;

public class UserOnly extends User {

    public UserOnly() {
    }

    public UserOnly(String id, String username, String email, String name) {
        super(id, username, email, name);
    }

    @Override
    public org.acme.crudReactiveHibernate.data.entity.User toDto() {
        return new org.acme.crudReactiveHibernate.data.entity.User(getId(), getUsername(), getEmail(), getName());
    }

    public static UserOnly fromDto(org.acme.crudReactiveHibernate.data.entity.User user) {
        UserOnly nUser = new UserOnly(user.getId(), user.getUsername(), user.getEmail(), user.getName());
        for (UserRole role : user.getRoles()) {
            if (nUser.rolesIds.containsKey(role.getRole().getId().getAppCode()))
                nUser.rolesIds.get(role.getRole().getId().getAppCode()).add(role.getRole().getId().getCode());
            else {
                nUser.rolesIds.put(role.getRole().getId().getAppCode(), new ArrayList<>(Collections.singletonList(role.getRole().getId().getCode())));
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

    @JsonProperty("roles_ids")
    private Map<String, List<String>> rolesIds = new HashMap<>();

    public Map<String, List<String>> getRolesIds() {
        return rolesIds;
    }

    public void setRolesIds(Map<String, List<String>> rolesIds) {
        this.rolesIds = rolesIds;
    }
}
