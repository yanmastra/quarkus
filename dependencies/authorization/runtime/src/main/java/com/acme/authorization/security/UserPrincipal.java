package com.acme.authorization.security;

import com.acme.authorization.json.UserOnly;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserPrincipal implements Principal {

    private final UserOnly user;
    @JsonProperty("allowed_roles")
    private final List<String> allowedRoles;

    public UserPrincipal(UserOnly user, List<String> allowedRoles) {
        this.user = user;
        this.allowedRoles = allowedRoles == null ? new ArrayList<>() : allowedRoles;
    }

    @Override
    public String getName() {
        return user.getName();
    }

    public boolean isRoleAllowed(String roleCode){
        return allowedRoles.contains(roleCode);
    }

    @Override
    public String toString() {
        return "UserPrincipal{" +
                "user=" + user +
                ", allowedRoles=" + allowedRoles +
                '}';
    }
}
