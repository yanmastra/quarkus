package com.acme.authorization.security;

import com.acme.authorization.json.UserOnly;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

public class UserPrincipal implements Principal {

    private final UserOnly user;
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
}
