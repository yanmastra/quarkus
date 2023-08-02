package org.acme.crudReactiveHibernate.data.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.runtime.util.StringUtil;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import org.hibernate.annotations.*;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "permission", indexes = {
        @Index(name = "_deleted_search", columnList = "deleted_at"),
        @Index(name = "_unique", columnList = "app_code, code", unique = true)
})
@SQLDelete(sql = "UPDATE permission SET deleted_at=NOW() WHERE id=?")
@FilterDefs(value = {
        @FilterDef(name = "deletedPermissionFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class)),
        @FilterDef(name = "publicPermission", parameters = @ParamDef(name = "isPublic", type = Boolean.class))
})
@Filters(
        value = {
                @Filter(name = "deletedPermissionFilter", condition = "deleted_at is null = :isDeleted"),
                @Filter(name = "publicPermission", condition = "deleted_at is null and is_public=:isPublic")
        }
)
public class Permission extends PanacheEntityBase implements Serializable {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "code", length = 36, nullable = false)
    private String code;

    @Column(length = 72, nullable = false)
    private String name;

    @Column(name = "app_code", nullable = false)
    private String appCode;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "deleted_at")
    private Date deletedAt;
    @Column(name = "deleted_by", length = 64)
    private String deletedBy;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;
    @Column(name = "created_by", length = 64)
    private String createdBy;

    public Permission() {
    }

    public Permission(String appCode, String code, String name) {
        this.code = code;
        this.name = name;
        this.appCode = appCode;
    }

    public Permission(String id, String appCode, String code, String name, Date deletedAt, String deletedBy) {
        this(appCode, code, name);
        this.id = id;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    @PrePersist
    void onInsert() {
        if (StringUtil.isNullOrEmpty(id)) {
            id = UUID.randomUUID().toString();
        }
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

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
