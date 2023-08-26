package com.acme.authorization.security;

import jakarta.ws.rs.core.SecurityContext;

public class UserSecurityContext implements SecurityContext {
    private final UserPrincipal principal;

    public UserSecurityContext(UserPrincipal principal) {
        this.principal = principal;
    }

    public UserSecurityContext() {
        principal = null;
    }

    @Override
    public UserPrincipal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(String s) {
        return principal != null && principal.isRoleAllowed(s);
    }

    @Override
    public boolean isSecure() {
        return principal != null;
    }

    @Override
    public String getAuthenticationScheme() {
        return "OAuth2";
    }
}
