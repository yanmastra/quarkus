package org.acme.authenticationService.dao.web;


import com.acme.authorization.json.UserOnly;

public class BaseModel {

    public UserOnly user;
    public String appName;

    public BaseModel() {
    }

    public BaseModel(UserOnly user, String appName) {
        this.user = user;
        this.appName = appName;
    }
}
