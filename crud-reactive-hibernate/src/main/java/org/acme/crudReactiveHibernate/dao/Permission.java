package org.acme.crudReactiveHibernate.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Permission implements Serializable {
    @JsonProperty("id")
    private String id;
    @JsonProperty("app_code")
    private String appCode;

    @JsonProperty("code")
    private String code;
    @JsonProperty("name")
    private String name;

    @JsonIgnore
    public org.acme.crudReactiveHibernate.data.entity.Permission toDTO() {
        return new org.acme.crudReactiveHibernate.data.entity.Permission(id, code, appCode, name);
    }

    public static Permission fromDTO(org.acme.crudReactiveHibernate.data.entity.Permission permission) {
        return new Permission(permission.getCode(), permission.getName());
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }
}
