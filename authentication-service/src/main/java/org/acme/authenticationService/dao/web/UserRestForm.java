package org.acme.authenticationService.dao.web;

import org.jboss.resteasy.reactive.RestForm;

public class UserRestForm {
    @RestForm("username")
    public String username;
    @RestForm("email")
    public String email;
    @RestForm("name")
    public String name;
    @RestForm("phone")
    public String phone;
    @RestForm("application_code")
    public String appCode;
    @RestForm("role_code")
    public String roleCode;

    @Override
    public String toString() {
        return "UserRestForm{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", appCode='" + appCode + '\'' +
                ", roleCode='" + roleCode + '\'' +
                '}';
    }
}
