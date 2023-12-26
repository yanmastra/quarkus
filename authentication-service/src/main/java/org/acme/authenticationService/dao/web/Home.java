package org.acme.authenticationService.dao.web;

import java.util.List;

public class Home extends BaseModel{
    public long applicationCount = 0;
    public long userCount = 0;
    public long roleCount = 0;
    public long permissionCount = 0;
    public List<String> childAppCodes;

    public Home() {
    }
}
