package org.acme.authenticationService.services;

import org.acme.authorization.json;
import com.acme.authorization.json.Role;

public interface Validator {

    static boolean validateRole(Role role) {
        if (role == null) return false;
        return !"SYSTEM".equalsIgnoreCase(role.getCode()) &&
                !"ROOT".equalsIgnoreCase(role.getCode());
    }

    static boolean validatePermission(Permission permission) {
        if (permission == null) return false;
        return !"SYSTEM".equalsIgnoreCase(permission.getCode()) &&
                !"ROOT".equalsIgnoreCase(permission.getCode());
    }
}
