package com.acme.authorization.security;

import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;

public class UserSecurityContext implements SecurityContext {
    private final UserPrincipal principal;

    public UserSecurityContext(UserPrincipal principal) {
        this.principal = principal;
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(String s) {
        return principal.isRoleAllowed(s);
    }

    @Override
    public boolean isSecure() {
        return principal != null;
    }

    @Override
    public String getAuthenticationScheme() {
        return "JWT";
    }
}
