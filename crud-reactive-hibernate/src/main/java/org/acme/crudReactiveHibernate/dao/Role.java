package org.acme.crudReactiveHibernate.dao;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public abstract class Role {
    @JsonProperty("app_code")
    private String appCode;
    @JsonProperty("code")
    private String code;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;

    @JsonProperty("created_at")
    private Date createdAt;
    @JsonProperty("created_by")
    private String createdBy;
    @JsonProperty("updated_at")
    private Date updatedAt;
    @JsonProperty("updated_by")
    private String updatedBy;
    @JsonProperty("deleted_at")
    private Date deletedAt;
    @JsonProperty("deleted_by")
    private String deletedBy;

    public Role() {
    }

    public Role(String code, String appCode, String name, String description) {
        this.code = code;
        this.appCode = appCode;
        this.name = name;
        this.description = description;
    }

    public abstract org.acme.crudReactiveHibernate.data.entity.Role toDto();
}
