package org.acme.authenticationService.dao.web;

import org.jboss.resteasy.reactive.RestForm;

public class PermissionRestForm {
    @RestForm("code")
    public String code;
    @RestForm("name")
    public String name;
    @RestForm("app_code")
    public String appCode;

    @Override
    public String toString() {
        return "RoleRestForm{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", appCode='" + appCode + '\'' +
                '}';
    }
}
