package com.acme.authorization.security;

import org.acme.authorization.json.UserOnly;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserPrincipal implements Principal {
    @JsonProperty("user")
    private final UserOnly user;
    @JsonProperty("allowed_roles")
    private final List<String> allowedRoles;

    @JsonProperty("app_code")
    private final String appCode;
    @JsonProperty("access_token")
    private final String accessToken;

    @JsonProperty("firebase_token")
    private final String firebaseToken;

    public UserPrincipal(UserOnly user, List<String> allowedRoles, String appCode, String accessToken) {
        this.user = user;
        this.allowedRoles = allowedRoles == null ? new ArrayList<>() : allowedRoles;
        this.appCode = appCode;
        this.accessToken = accessToken;
        this.firebaseToken = null;
    }

    public UserPrincipal(UserOnly user, List<String> allowedRoles, String appCode, String accessToken, String firebaseToken) {
        this.user = user;
        this.allowedRoles = allowedRoles == null ? new ArrayList<>() : allowedRoles;
        this.appCode = appCode;
        this.accessToken = accessToken;
        this.firebaseToken = firebaseToken;
    }

    @Override
    public String getName() {
        return user.getUsername();
    }

    @JsonIgnore
    public String getAppCode() {
        return appCode;
    }

    @JsonIgnore
    public boolean isRoleAllowed(String roleCode){
        boolean result = allowedRoles.contains(roleCode);
        if (result) return true;
        throw new SecurityException("Access Denied!");
    }

    @JsonIgnore
    public String getAccessToken() {
        return accessToken;
    }


    @JsonIgnore
    @Override
    public String toString() {
        return "UserPrincipal{" +
                "user=" + user +
                ", allowedRoles=" + allowedRoles +
                ", appCode='" + appCode + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", firebaseToken='" + firebaseToken + '\'' +
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
        return Objects.hash(user, allowedRoles, accessToken, firebaseToken);
    }

    @JsonIgnore
    public UserOnly getUser() {
        return user;
    }

    public static UserPrincipal valueOf(Principal principal) {
        if (principal instanceof UserPrincipal up) {
            return up;
        }
        throw new IllegalArgumentException("The user principal is not secured");
    }

    public static UserPrincipal valueOf(SecurityContext context) {
        if (context instanceof UserSecurityContext usc) {
            return usc.getUserPrincipal();
        }
        throw new IllegalArgumentException("The user principal is not secured");
    }

    public static UserPrincipal valueOf(ContainerRequestContext context) {
        SecurityContext securityContext = context.getSecurityContext();
        if (securityContext instanceof UserSecurityContext usc) {
            return usc.getUserPrincipal();
        }
        throw new IllegalArgumentException("The user principal is not secured");
    }

    public String getFirebaseToken() {
        return firebaseToken;
    }
}
