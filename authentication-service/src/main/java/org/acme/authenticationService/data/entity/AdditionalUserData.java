package org.acme.authenticationService.data.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "additional_user_data", indexes = @Index(
        name = "_unique",
        columnList = "app_code, user_id, field_code",
        unique = true
))
public class AdditionalUserData extends PanacheEntityBase implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;
    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private AuthUser user;
    @Column(name = "app_code", length = 36, nullable = false)
    private String appCode;
    @Column(name = "field_code", length = 36, nullable = false)
    private String fieldCode;
    @Column(name = "value")
    private String value;

    public AdditionalUserData() {
    }

    public AdditionalUserData(String id, AuthUser user, String appCode, String fieldCode, String value) {
        this.id = id;
        this.user = user;
        this.appCode = appCode;
        this.fieldCode = fieldCode;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AuthUser getUser() {
        return user;
    }

    public void setUser(AuthUser user) {
        this.user = user;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getFieldCode() {
        return fieldCode;
    }

    public void setFieldCode(String fieldCode) {
        this.fieldCode = fieldCode;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
