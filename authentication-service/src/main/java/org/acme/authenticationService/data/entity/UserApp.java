package org.acme.authenticationService.data.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "user_app", indexes = @Index(name = "_unique_id", columnList = "user_id, app_code", unique = true))
public class UserApp extends PanacheEntityBase implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;
    //    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @Column(name = "user_id", length = 40, nullable = false)
    private String userId;
    //    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @Column(name = "app_code", length = 40, nullable = false)
    private String appCode;

    public UserApp() {
    }

    public UserApp(AuthUser user, Application application) {
        if (user == null || application == null) throw new IllegalArgumentException("AuthUser and Application can't be null!");
        this.userId = user.getId();
        this.appCode = application.getCode();
    }

    public UserApp(String userId, String appCode) {
        this.userId = userId;
        this.appCode = appCode;
    }
//
//    public AuthUser getUser() {
//        return user;
//    }
//
//    public void setUser(AuthUser user) {
//        this.user = user;
//    }
//
//    public Application getApplication() {
//        return application;
//    }
//
//    public void setApplication(Application application) {
//        this.application = application;
//    }

    @Override
    public String toString() {
        return "UserApp{" +
                "id='"+id+"'," +
                "user=" + userId +
                ", appCode=" + appCode +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }
}
