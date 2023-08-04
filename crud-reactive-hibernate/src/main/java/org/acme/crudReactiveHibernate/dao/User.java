package org.acme.crudReactiveHibernate.dao;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class User extends com.acme.authorization.json.User implements Serializable {

    public abstract org.acme.crudReactiveHibernate.data.entity.User toDto();

    public User() {
    }

    public User(String id, String username, String email, String name) {
        super(id, username, email, name);
    }
}
