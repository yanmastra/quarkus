package org.acme.authenticationService.data.entity;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Embeddable
public class UserRoleId {
    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private AuthUser authUser;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "app_code", referencedColumnName = "app_code", nullable = false),
            @JoinColumn(name = "role_code", referencedColumnName = "code", nullable = false)
    })
    private Role role;


    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (authUser == null || role == null) return false;
        if (obj instanceof UserRoleId pObj) {
            return authUser.equals(pObj.authUser) && role.equals(pObj.role);
        }
        return false;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        hcb.append(authUser);
        hcb.append(role);
        return hcb.toHashCode();
    }

    public UserRoleId() {
    }

    public UserRoleId(AuthUser authUser, Role role) {
        this.authUser = authUser;
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public AuthUser getUser() {
        return authUser;
    }

    public void setUser(AuthUser authUser) {
        this.authUser = authUser;
    }

    @Override
    public String toString() {
        return "UserRoleId{" +
                "authUser=" + authUser +
                ", role=" + role +
                '}';
    }
}
