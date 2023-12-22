package org.acme.authenticationService.dao.web;

import org.jboss.resteasy.reactive.RestForm;

import java.util.Arrays;

public class RoleAddPermissionRestForm {
    @RestForm("permissions")
    public String[] permissions;

    @Override
    public String toString() {
        return "RoleAddPermissionRestForm{" +
                "permissions=" + Arrays.toString(permissions) +
                '}';
    }
}
