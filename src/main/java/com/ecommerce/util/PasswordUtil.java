package com.ecommerce.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    private static final int ROUNDS = 12;

    public static String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(ROUNDS));
    }

    public static boolean verify(String plain, String hashed) {
        return BCrypt.checkpw(plain, hashed);
    }
}
