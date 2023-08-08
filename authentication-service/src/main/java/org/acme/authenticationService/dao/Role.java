package org.acme.authenticationService.dao;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Role extends com.acme.authorization.json.Role {

    public Role() {
    }

    public Role(String id, String code, String appCode, String name, String description) {
        super(id, code, appCode, name, description);
    }

    abstract org.acme.authenticationService.data.entity.Role toDto();
}
