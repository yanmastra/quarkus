package org.acme.authenticationService.dao.web;

public class Login extends BaseModel {
    public String loginId;
    public boolean isRedirect = false;
    public String errorMessage;

    public Login(String appName, String loginId, boolean isRedirect, String errorMessage) {
        this.appName = appName;
        this.loginId = loginId;
        this.isRedirect = isRedirect;
        this.errorMessage = errorMessage;
    }

    public Login() {
    }
}
