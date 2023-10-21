package org.acme.authenticationService.dao.web;

import com.acme.authorization.json.UserOnly;
import org.acme.authenticationService.dao.ApplicationJson;

import java.util.List;

public class ApplicationDetailModel extends BaseModel{

    public ApplicationJson application;
    public List<ApplicationJson> data;
    public boolean isAlert = false;
    public boolean isAlertSuccess;
    public String alertMessage;

    public ApplicationDetailModel(UserOnly user, String appName, ApplicationJson application, List<ApplicationJson> children) {
        super(user, appName);
        this.application = application;
        this.data = children;
    }

    public ApplicationDetailModel() {
    }
}
