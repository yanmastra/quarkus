package org.acme.authenticationService.dao.web;

import org.acme.authorization.json.UserOnly;
import org.acme.authenticationService.dao.Permission;
import org.acme.authenticationService.dao.RoleOnly;

import java.util.Map;

public class RoleFormAddPermissionModel extends BaseModel {
    public RoleOnly role;
    public Map<String, Permission> rolePermissions;
    public Map<String, Permission> permissions;

    public RoleFormAddPermissionModel(UserOnly user, String appName) {
        super(user, appName);
    }
}
