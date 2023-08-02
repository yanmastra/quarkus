package org.acme.crudReactiveHibernate.data.entity;

import io.netty.util.internal.StringUtil;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import org.hibernate.annotations.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "role", indexes = {
        @Index(name = "_unique", columnList = "app_code, code", unique = true),
        @Index(name = "_deleted_search", columnList = "deleted_at"),
})
@SQLDelete(sql = "UPDATE user SET deleted_at=NOW() WHERE id=?")
@FilterDef(name = "deletedRoleFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
@Filter(name = "deletedRoleFilter", condition = "deleted_at is null = :isDeleted")
public class Role extends PanacheEntityBase implements Serializable {
    @EmbeddedId
    private RoleId id;
    @Column(length = 72, nullable = false)
    private String name;
    @Column(length = 128)
    private String description;

    @OneToMany(mappedBy = "id.role", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<RolePermission> permissions = new HashSet<>();

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

    public Role() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj instanceof Role pObj) {
            return id.equals(pObj.id);
        }
        return false;
    }

    public Role(RoleId id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    @PrePersist
    private void validateData() {
        if (!StringUtil.isNullOrEmpty(getId().getAppCode())) {
            for (RolePermission rp: permissions) {
                if (!StringUtil.isNullOrEmpty(rp.getRole().getId().getAppCode()))
                    rp.getRole().getId().setAppCode(getId().getAppCode());
            }
        }
    }

    public void addPermission(Permission permission) {
        if (permissions == null) permissions = new HashSet<>();
        RolePermission rp = new RolePermission(this, permission);
        permissions.add(rp);
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public RoleId getId() {
        return id;
    }

    public void setId(RoleId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<RolePermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Collection<RolePermission> permissions) {
        this.permissions = new HashSet<>(permissions);
    }

    public void setPermissions(Set<RolePermission> permissions) {
        this.permissions = permissions;
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
}
