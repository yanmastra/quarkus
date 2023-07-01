package org.acme.crudReactiveHibernate.data.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "permission", indexes = {
        @Index(name = "_unique", columnList = "app_code,code", unique = true),
        @Index(name = "_deleted_search", columnList = "deleted_at"),
})
@SQLDelete(sql = "UPDATE permission SET deleted_at=NOW() WHERE id=?")
@FilterDef(name = "deletedPermissionFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
@Filter(name = "deletedPermissionFilter", condition = "deleted_at is null = :isDeleted")
public class Permission extends PanacheEntityBase implements Serializable {
    @Id
    @Column(length = 40, nullable = false)
    private String id;
    @Column(name = "code", length = 36, nullable = false)
    private String code;
    @Column(name = "app_code", length = 16, nullable = false)
    private String appCode;

    @Column(length = 72, nullable = false)
    private String name;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "deleted_at")
    private Date deletedAt;
    @Column(name = "deleted_by", length = 64)
    private String deletedBy;

    @OneToMany(mappedBy = "id.permission", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RolePermission> rolePermissions = null;

    public Permission() {
    }

    public Permission(String id, String code, String appCode, String name) {
        this.id = id;
        this.code = code;
        this.appCode = appCode;
        this.name = name;
    }

    @PrePersist
    private void generateUUID() {
        if (id == null) {
            id = "PMS_" + UUID.randomUUID().toString().toUpperCase();
        }
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAppCode() {
       return appCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
