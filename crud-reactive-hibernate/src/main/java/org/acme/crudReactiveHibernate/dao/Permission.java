package org.acme.crudReactiveHibernate.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Permission implements Serializable {
    @JsonProperty("id")
    private String id;
    @JsonProperty("code")
    private String code;
    @JsonProperty("app_code")
    private String appCode;
    @JsonProperty("name")
    private String name;

    @JsonProperty("deleted_at")
    private Date deletedAt;
    @JsonProperty("deleted_by")
    private String deletedBy;

    @JsonIgnore
    public org.acme.crudReactiveHibernate.data.entity.Permission toDTO() {
        return new org.acme.crudReactiveHibernate.data.entity.Permission(id, appCode, code, name, deletedAt, deletedBy);
    }

    public static Permission fromDTO(org.acme.crudReactiveHibernate.data.entity.Permission permission) {
        Permission dao = new Permission(permission.getCode(), permission.getName());
        dao.setId(permission.getId());
        dao.setAppCode(permission.getAppCode());
        dao.setDeletedAt(permission.getDeletedAt());
        dao.setDeletedBy(permission.getDeletedBy());
        return dao;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj instanceof Permission pObj) {
            return code.equals(pObj.code);
        }
        return false;
    }

    public Permission() {
    }

    public Permission(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
