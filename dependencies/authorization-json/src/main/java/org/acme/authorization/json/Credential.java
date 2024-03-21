package org.acme.authorization.json;

import java.util.Date;

public interface Credential {

    String getUsername();

    void setUsername(String username);

    String getPassword();

    void setPassword(String password);

    String getAppCode();

    void setAppCode(String appCode);

    Date getExpToken();

    void setExpToken(Date expToken);
}
