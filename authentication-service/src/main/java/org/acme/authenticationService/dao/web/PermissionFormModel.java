package org.acme.authenticationService.dao.web;

import com.acme.authorization.json.UserOnly;
import org.acme.authenticationService.dao.ApplicationJson;

import java.util.List;

public class PermissionFormModel extends BaseModel {
    public List<ApplicationJson> apps;

    public PermissionFormModel(UserOnly user, String appName) {
        super(user, appName);
    }
}
