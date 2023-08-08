package org.acme.authenticationService.data.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import net.acintya.keyGen.Generator;
import org.hibernate.annotations.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "application")
@SQLDelete(sql = "UPDATE application SET deleted_at=NOW() WHERE id=?")
@FilterDef(name = "deletedAppFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
@Filter(name = "deletedAppFilter", condition = "deleted_at is not null = :isDeleted")
public class Application extends PanacheEntityBase implements Serializable {
    @Id
    @Column(length = 16, nullable = false)
    private String code;
    @Column(length = 128)
    private String name;
    @Column(name = "secret_key")
    private String secretKey;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_app_code", referencedColumnName = "code")
    private Application parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Application> children;

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
    public void generateSecretKey() {
        secretKey = Generator.generatePassword(32, true);
    }

    public Application() {
    }

    public Application(String code, String name) {
        this.code = code;
        this.name = name;
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

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Application getParent() {
        return parent;
    }

    public void setParent(Application parent) {
        this.parent = parent;
    }

    public List<Application> getChildren() {
        return children;
    }

    public void setChildren(List<Application> children) {
        this.children = children;
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
