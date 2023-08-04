package org.acme.crudReactiveHibernate.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Permission extends com.acme.authorization.json.Permission implements Serializable {

    @JsonIgnore
    public org.acme.crudReactiveHibernate.data.entity.Permission toDTO() {
        return new org.acme.crudReactiveHibernate.data.entity.Permission(getId(), getAppCode(), getCode(), getName(), getDeletedAt(), getDeletedBy());
    }

    public static Permission fromDTO(org.acme.crudReactiveHibernate.data.entity.Permission permission) {
        Permission dao = new Permission(permission.getCode(), permission.getName());
        dao.setId(permission.getId());
        dao.setAppCode(permission.getAppCode());
        dao.setDeletedAt(permission.getDeletedAt());
        dao.setDeletedBy(permission.getDeletedBy());
        return dao;
    }

    public Permission() {
    }

    public Permission(String code, String name) {
        super(code, name);
    }
}
