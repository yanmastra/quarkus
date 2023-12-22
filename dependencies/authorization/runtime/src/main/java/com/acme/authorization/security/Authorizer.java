package com.acme.authorization.security;

public interface Authorizer {
    /**
     * Deprecated due to new proper method, use Authorizer.authorize(String accessToken,
     * @param accessToken
     * @return
     */
    @Deprecated
    UserPrincipal authorize(String accessToken);
    int getPriority();
}
