package org.acme.authenticationService.dao.web;

import org.acme.authenticationService.dao.RoleOnly;
import org.acme.authenticationService.dao.UserOnly;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserPageModel extends BaseModel{
    public List<UserOnly> data;
    public Map<String, String> roleNames = new HashMap<>();
    public Map<String, List<RoleOnly>> roles = new HashMap<>();
}
