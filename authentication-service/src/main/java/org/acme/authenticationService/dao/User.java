package org.acme.authenticationService.dao;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.acme.authenticationService.data.entity.AuthUser;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class User extends com.acme.authorization.json.User implements Serializable {

    public abstract AuthUser toDto();

    public User() {
    }

    public User(String id, String username, String email, String name) {
        super(id, username, email, name);
    }
}
