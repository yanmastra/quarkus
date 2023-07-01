package org.acme.crudReactiveHibernate.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class User implements Serializable {
    @JsonProperty("id")
    private String id;
    @JsonProperty("username")
    private String username;
    @JsonProperty("password")
    private String password;
    @JsonProperty("email")
    private String email;

    @JsonProperty("roles")
    private List<Role> roles;

    @JsonIgnore
    public org.acme.crudReactiveHibernate.data.entity.User toDto() {
        org.acme.crudReactiveHibernate.data.entity.User user = new org.acme.crudReactiveHibernate.data.entity.User(id, username, email, password);
        if (roles != null) {
            for (Role role: roles) {
                user.addRole(role.toDTO());
            }
        }
        return user;
    }

    public static User fromDto(org.acme.crudReactiveHibernate.data.entity.User user) {
        User nUser = new User(user.getId(), user.getUsername(), user.getPassword(), user.getEmail());
        if (user.getRoles() != null) {
            nUser.roles = new ArrayList<>();
            for (org.acme.crudReactiveHibernate.data.entity.UserRole ur: user.getRoles()) {
                nUser.roles.add(Role.fromDTO(ur.getRole()));
            }
        }
        return nUser;
    }

    public User() {
    }

    public User(String id, String username, String password, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
