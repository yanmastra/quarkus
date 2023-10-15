package com.acme.authorization.utils;

import java.util.regex.Pattern;

public class ValidationUtils {
    private ValidationUtils(){}

    public static boolean isEmail(String email) {
        return Pattern.compile("^[a-zA-Z0-9_! #$%&'*+/=?`{|}~^. -]+@[a-zA-Z0-9. -]+$")
                .matcher(email)
                .matches();
    }
}
