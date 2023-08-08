package com.acme.authorization.utils;

import java.util.Random;

public class PasswordGenerator {
    private final char[] SPECIAL_CHARACTERS = new char[]{'!', '@', '#', '$', '?', '%', '*', '&'};
    private final char[] ALPHABET = new char[26];
    private final Random random = new Random();
    private boolean initialized = false;

    private PasswordGenerator() {
    }

    private void init() {
        if (!this.initialized) {
            int i = 0;

            for(char ch = 'a'; ch <= 'z'; ++ch) {
                this.ALPHABET[i] = ch;
                ++i;
            }

            this.initialized = true;
        }
    }

    private char getAlphabet() {
        return this.ALPHABET[this.random.nextInt(26)];
    }

    private char getSpecial() {
        return this.SPECIAL_CHARACTERS[this.random.nextInt(this.SPECIAL_CHARACTERS.length)];
    }

    public static String generatePassword(int length, boolean includeSpecial) {
        PasswordGenerator passwordGenerator = new PasswordGenerator();
        passwordGenerator.init();
        byte[] result = new byte[length];

        for(int i = 0; i < result.length; ++i) {
            int act = passwordGenerator.random.nextInt(4);
            result[i] = (byte)(act == 0 ? passwordGenerator.getAlphabet() : (act == 1 ? (includeSpecial ? passwordGenerator.getSpecial() : passwordGenerator.getAlphabet()) : (act == 2 ? (new String(new byte[]{(byte) passwordGenerator.getAlphabet()})).toUpperCase().getBytes()[0] : ("" + passwordGenerator.random.nextInt(9)).getBytes()[0])));
        }

        return new String(result);
    }
}