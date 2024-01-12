package org.acme.authenticationService.dao;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterRequestJson implements Serializable {
    @JsonProperty("name")
    private String name;
    @JsonProperty("email")
    private String email;
    @JsonProperty("phone")
    private String phone;
    @JsonProperty("process_id")
    private String processId;
    @JsonProperty("role_code")
    private String roleCode;
    @JsonProperty("application_code")
    private String applicationCode;
    @JsonProperty("otp_key")
    private String otpKey;
    @JsonProperty("otp_code")
    private String otpCode;

    public RegisterRequestJson(){}

    public String getContact() {
        if (StringUtils.isBlank(email) && StringUtils.isBlank(phone)) throw new IllegalArgumentException("Email and Phone number couldn't be empty!. One of them should be filled with correct value!");
        if (StringUtils.isNotBlank(email)) return email;
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcessId() {
        return processId;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getApplicationCode() {
        return applicationCode;
    }

    public void setApplicationCode(String applicationCode) {
        this.applicationCode = applicationCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOtpKey() {
        return otpKey;
    }

    public void setOtpKey(String otpKey) {
        this.otpKey = otpKey;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    @Override
    public String toString() {
        return "RegisterRequestJson{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", processId='" + processId + '\'' +
                ", roleCode='" + roleCode + '\'' +
                ", applicationCode='" + applicationCode + '\'' +
                '}';
    }
}
