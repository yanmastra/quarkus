package org.acme.authenticationService.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.acme.authenticationService.data.entity.AuthUser;
import org.acme.authenticationService.data.entity.UserRole;

import java.util.*;

public class UserOnly extends User {

    public UserOnly() {
    }

    public UserOnly(String id, String username, String email, String name) {
        super(id, username, email, name);
    }

    @Override
    public AuthUser toDto() {
        return new AuthUser(getId(), getUsername(), getEmail(), getName());
    }

    public static UserOnly fromDto(AuthUser authUser) {
        UserOnly nUser = new UserOnly(authUser.getId(), authUser.getUsername(), authUser.getEmail(), authUser.getName());
        for (UserRole role : authUser.getRoles()) {
            if (nUser.rolesIds.containsKey(role.getRole().getAppCode()))
                nUser.rolesIds.get(role.getRole().getAppCode()).add(role.getRole().getCode());
            else {
                nUser.rolesIds.put(role.getRole().getAppCode(), new ArrayList<>(Collections.singletonList(role.getRole().getCode())));
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

    @JsonProperty("roles_ids")
    private Map<String, List<String>> rolesIds = new HashMap<>();

    public Map<String, List<String>> getRolesIds() {
        return rolesIds;
    }

    public void setRolesIds(Map<String, List<String>> rolesIds) {
        this.rolesIds = rolesIds;
    }
}
