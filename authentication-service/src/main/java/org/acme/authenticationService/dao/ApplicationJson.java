package org.acme.authenticationService.dao;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.acme.authenticationService.data.entity.Application;
import org.jboss.resteasy.reactive.RestForm;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationJson {

    @JsonProperty("code")
    private String code;
    @RestForm("name")
    @JsonProperty("name")
    private String name;
    @JsonProperty("secret_key")
    private String secretKey;
    @RestForm("parent_code")
    private String parentCode;
    @RestForm("description")
    @JsonProperty("description")
    private String description;

    @JsonProperty( "parent")
    private ApplicationJson parent;

    @JsonProperty( "created_at")
    private Date createdAt;
    @JsonProperty( "created_by")
    private String createdBy;
    @JsonProperty( "updated_at")
    private Date updatedAt;
    @JsonProperty( "updated_by")
    private String updatedBy;
    @JsonProperty( "deleted_at")
    private Date deletedAt;
    @JsonProperty( "deleted_by")
    private String deletedBy;

    public static ApplicationJson fromDto(Application application) {
        if (application == null) return null;
        return new ApplicationJson(
                application.getCode(),
                application.getName(),
                application.getSecretKey(),
                ApplicationJson.fromDto(application.getParent()),
                application.getCreatedAt(),
                application.getCreatedBy(),
                application.getUpdatedAt(),
                application.getUpdatedBy()
        );
    }

    public ApplicationJson() {
    }

    public ApplicationJson(String code, String name, String secretKey, ApplicationJson parent, Date createdAt, String createdBy, Date updatedAt, String updatedBy) {
        this.code = code;
        this.name = name;
        this.secretKey = secretKey;
        this.parent = parent;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
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

    public ApplicationJson getParent() {
        return parent;
    }

    public void setParent(ApplicationJson parent) {
        this.parent = parent;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }
}
