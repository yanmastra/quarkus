package org.acme.authenticationService.dao.web;

import com.acme.authorization.json.UserOnly;
import org.acme.authenticationService.dao.ApplicationJson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationModel extends BaseModel {
    public ApplicationModel() {
    }

    public ApplicationModel(UserOnly user, String appName) {
        super(user, appName);
    }

    public List<ApplicationJson> data;
    public Map<String, Long> childAppCount = new HashMap<>();
    public boolean isAlert = false;
    public boolean isAlertSuccess;
    public String alertMessage;
}
