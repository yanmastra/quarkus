package org.acme.authenticationService.dao.web;

import org.acme.authorization.json.UserOnly;
import org.acme.authenticationService.dao.Permission;

import java.util.List;

public class PermissionPageModel extends BaseModel{

    public PermissionPageModel(UserOnly user, String appName) {
        super(user, appName);
    }

    public PermissionPageModel() {
    }

    public List<Permission> data;
}
