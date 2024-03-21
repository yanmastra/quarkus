package org.acme.authorization.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserOnly extends User implements Serializable {

    public UserOnly() {
    }

    public UserOnly(String id, String username, String email, String name) {
        super(id, username, email, name);
    }

    @JsonProperty("roles_ids")
    private Map<String, List<String>> rolesIds = new HashMap<>();

    @JsonIgnore
    public Map<String, List<String>> getRolesIds() {
        return rolesIds;
    }

    public void setRolesIds(Map<String, List<String>> rolesIds) {
        this.rolesIds = rolesIds;
    }
}
