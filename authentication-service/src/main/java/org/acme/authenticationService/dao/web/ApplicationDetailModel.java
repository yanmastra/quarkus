package org.acme.authenticationService.dao.web;

import org.acme.authorization.json.UserOnly;
import org.acme.authenticationService.dao.ApplicationJson;
import org.acme.authenticationService.dao.RoleOnly;

import java.util.List;

public class ApplicationDetailModel extends BaseModel{

    public ApplicationJson application;
    public List<ApplicationJson> data;
    public List<RoleOnly> roles;

    public ApplicationDetailModel(UserOnly user, String appName, ApplicationJson application, List<ApplicationJson> children) {
        super(user, appName);
        this.application = application;
        this.data = children;
    }

    public ApplicationDetailModel() {
    }
}
