package org.acme.crudReactiveHibernate.data.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity
@Table(name = "user_role")
public class UserRole implements Serializable {
    @EmbeddedId
    private final UserRoleId id;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj instanceof UserRole pObj) {
            return id.equals(pObj.id);
        }
        return false;
    }

    public UserRole() {
        id = new UserRoleId();
    }

    public UserRole(User user, Role role) {
        id = new UserRoleId(user, role);
    }

    public Role getRole() {
        return id.getRole();
    }

    public void setRole(Role role) {
        this.id.setRole(role);
    }


    public User getUser() {
        return id.getUser();
    }

    public void setUser(User user) {
        this.id.setUser(user);
    }
}
