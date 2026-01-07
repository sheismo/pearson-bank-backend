package com.zainab.PearsonBank.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class PasswordGenerator {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#%&*()=+";

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{12,}$"
    );

    private static final Pattern PIN_PATTERN = Pattern.compile(
            "^\\d{4}$"
    );

    private static final SecureRandom random = new SecureRandom();

    public String generatePassword() {
        int length = 12;

        List<Character> passwordChars = new ArrayList<>();

        // guarantee required characters
        passwordChars.add(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        passwordChars.add(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        passwordChars.add(DIGITS.charAt(random.nextInt(DIGITS.length())));
        passwordChars.add(SYMBOLS.charAt(random.nextInt(SYMBOLS.length())));

        String allChars = UPPERCASE + LOWERCASE + DIGITS + SYMBOLS;

        // fill remaining characters
        for (int i = passwordChars.size(); i < length; i++) {
            passwordChars.add(allChars.charAt(random.nextInt(allChars.length())));
        }

        // shuffle to avoid predictable order
        Collections.shuffle(passwordChars, random);

        // convert to String
        StringBuilder password = new StringBuilder();
        for (char c : passwordChars) {
            password.append(c);
        }

        return password.toString();
    }

    public boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isValidPin(String pin) {
        if (pin == null) {
            return false;
        }
        return PIN_PATTERN.matcher(pin).matches();
    }
}

