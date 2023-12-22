package org.acme.authenticationService.dao.web;

import org.jboss.resteasy.reactive.RestForm;

public class RoleRestForm {
    @RestForm("role_code")
    public String code;
    @RestForm("name")
    public String name;
    @RestForm("app_code")
    public String appCode;
    @RestForm("description")
    public String description;

    @Override
    public String toString() {
        return "RoleRestForm{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", appCode='" + appCode + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
