package com.acme.authorization.security;

import com.acme.authorization.json.UserOnly;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserPrincipal implements Principal {

    private final UserOnly user;
    @JsonProperty("allowed_roles")
    private final List<String> allowedRoles;

    private final String appCode;

    public UserPrincipal(UserOnly user, List<String> allowedRoles, String appCode) {
        this.user = user;
        this.allowedRoles = allowedRoles == null ? new ArrayList<>() : allowedRoles;
        this.appCode = appCode;
    }

    @Override
    public String getName() {
        return user.getUsername();
    }

    public String getAppCode() {
        return appCode;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal principal = (UserPrincipal) o;
        return Objects.equals(user.getId(), principal.user.getId()) && Objects.equals(allowedRoles, principal.allowedRoles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, allowedRoles);
    }
}
