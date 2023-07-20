package org.acme.crudReactiveHibernate.data.entity;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Embeddable
public class UserRoleId {
    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

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
        if (user == null || role == null) return false;
        if (obj instanceof UserRoleId pObj) {
            return user.equals(pObj.user) && role.equals(pObj.role);
        }
        return false;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        hcb.append(user);
        hcb.append(role);
        return hcb.toHashCode();
    }

    public UserRoleId() {
    }

    public UserRoleId(User user, Role role) {
        this.user = user;
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
