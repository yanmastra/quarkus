package com.acme.authorization.security;

public interface Authorizer {
    UserPrincipal authorize(String accessToken);
    int getPriority();
}
