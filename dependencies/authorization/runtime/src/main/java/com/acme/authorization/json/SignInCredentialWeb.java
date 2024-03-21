package com.acme.authorization.json;

import org.acme.authorization.json.Credential;
import org.jboss.resteasy.reactive.RestForm;

import java.util.Date;

public class SignInCredentialWeb implements Credential {
    @RestForm("username")
    private String username;
    @RestForm("password")
    private String password;
    @RestForm("app_code")
    private String appCode;
    private Date expToken;

    public SignInCredentialWeb() {
    }

    public SignInCredentialWeb(String username, String password, String appCode) {
        this.username = username;
        this.password = password;
        this.appCode = appCode;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getAppCode() {
        return appCode;
    }

    @Override
    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    @Override
    public Date getExpToken() {
        return expToken;
    }

    @Override
    public void setExpToken(Date expToken) {
        this.expToken = expToken;
    }

    @Override
    public String toString() {
        return "SignInCredentialWeb{" +
                "username='" + username + '\'' +
                ", password='******'" +
                ", appCode='" + appCode + '\'' +
                ", expToken=" + expToken +
                '}';
    }
}
