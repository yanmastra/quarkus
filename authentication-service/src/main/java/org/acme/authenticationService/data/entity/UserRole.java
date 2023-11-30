package org.acme.authenticationService.data.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "user_role")
public class UserRole extends PanacheEntityBase implements Serializable {
    @Id
    @Column(length = 36, nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private AuthUser authUser;
    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false)
    private Role role;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj instanceof UserRole pObj) {
            return id.equals(pObj.id);
        }
        return false;
    }

    public UserRole() {}

    public UserRole(AuthUser authUser, Role role) {
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "UserRole{" +
                "id='" + id + '\'' +
                ", authUser=" + authUser +
                ", role=" + role +
                '}';
    }
}
