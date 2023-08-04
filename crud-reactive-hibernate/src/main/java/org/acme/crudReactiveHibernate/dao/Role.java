package org.acme.crudReactiveHibernate.dao;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Role extends com.acme.authorization.json.Role {

    public Role() {
    }

    public Role(String code, String appCode, String name, String description) {
        super(code, appCode, name, description);
    }

    abstract org.acme.crudReactiveHibernate.data.entity.Role toDto();
}
