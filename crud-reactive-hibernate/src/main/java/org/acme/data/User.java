package org.acme.data;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(
        name = "user",
        indexes = @Index(name = "search", columnList = "id, username, email")
)
public class User extends PanacheEntityBase {
    @Id
    @Column(length = 36)
    private String id;
    @Column(length = 64, unique = true, nullable = false)
    private String username;
    @Column(length = 64, unique = true)
    private String email;
    private String password;

    public User() {
    }

    public User(String id, String username, String email, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
