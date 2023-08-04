package com.acme.authorization.json;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserOnly extends User {

    public UserOnly() {
    }

    public UserOnly(String id, String username, String email, String name) {
        super(id, username, email, name);
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
