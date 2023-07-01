package org.acme.crudReactiveHibernate.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Embeddable
public class RoleId {
    @Column(name = "app_code", length = 16, nullable = false)
    private String appCode;
    @Column(name = "code", length = 36, nullable = false)
    private String code;


    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (code == null || appCode == null) return false;
        if (obj instanceof RoleId pObj) {
            return appCode.equals(pObj.appCode) && code.equals(pObj.code);
        }
        return false;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        hcb.append(appCode);
        hcb.append(code);
        return hcb.toHashCode();
    }

    public RoleId() {
    }

    public RoleId(String appCode, String code) {
        this.appCode = appCode;
        this.code = code;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
