package org.acme.authenticationService.data.entity;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import org.hibernate.annotations.*;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "auth_user",
        indexes = {
                @Index(name = "search", columnList = "id, username, email"),
                @Index(name = "_deleted_search", columnList = "deleted_at"),
        }
)
@SQLDelete(sql = "UPDATE auth_user SET deleted_at=NOW() WHERE id=?")
@SQLSelect(sql = "SELECT id, username, email, name, createdAt, createdBy, updatedAt, updatedBy, deletedAt, deletedBy FROM auth_user")
@FilterDefs({
        @FilterDef(name = "deletedUserFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class)),
        @FilterDef(name = "isActiveUser", parameters = @ParamDef(name = "isActive", type = Boolean.class)),
})
@Filters({
        @Filter(name = "deletedUserFilter", condition = "deleted_at is null = :isDeleted"),
        @Filter(name = "isActiveUser", condition = "deleted_at is null and password is not null = :isActive"),
})
public class AuthUser extends PanacheEntityBase implements Serializable {
    @Id
    @Column(length = 40, updatable = false)
    private String id;
    @Column(length = 64, unique = true, nullable = false)
    private String username;
    @Column(length = 64, unique = true)
    private String email;
    private String password;
    @Column(length = 218)
    private String name;

    @Transient
    private Set<UserRole> roles = null;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;
    @Column(name = "created_by", length = 64)
    private String createdBy;
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;
    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "deleted_at")
    private Date deletedAt;
    @Column(name = "deleted_by", length = 64)
    private String deletedBy;

    @PrePersist
    private void generateUUID() {
        if (id == null) {
            id = "USE_" + UUID.randomUUID().toString().toUpperCase();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (id == null) return false;
        if (obj instanceof AuthUser pObj) {
            return id.equals(pObj.id);
        }
        return false;
    }

    public AuthUser() {
    }

    public AuthUser(String id, String username, String email, String name) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.name = name;
    }

    public void addRole(Role role) {
        if (roles == null) roles = new HashSet<>();
        UserRole rp = new UserRole(this, role);
        roles.add(rp);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<UserRole> getRoles() {
        if (roles == null) roles = new HashSet<>();
        return roles;
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPasswordTextPlain(String passwordTextPlain) {
        this.password = BcryptUtil.bcryptHash(passwordTextPlain);
    }

    public boolean validatePassword(String plainText) {
        return BcryptUtil.matches(plainText, this.password);
    }

    @Override
    public String toString() {
        return "AuthUser{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                ", createdBy='" + createdBy + '\'' +
                ", updatedAt=" + updatedAt +
                ", updatedBy='" + updatedBy + '\'' +
                ", deletedAt=" + deletedAt +
                ", deletedBy='" + deletedBy + '\'' +
                '}';
    }
}
