package org.acme.authenticationService.dao.web;

import org.acme.authorization.json.UserOnly;
import org.acme.authenticationService.dao.ApplicationJson;
import org.acme.authenticationService.dao.RoleOnly;

import java.util.List;

public class RoleFormModel extends BaseModel {
    public List<ApplicationJson> apps;
    public List<RoleOnly> roles;

    public RoleFormModel(UserOnly user, String appName) {
        super(user, appName);
    }
}
