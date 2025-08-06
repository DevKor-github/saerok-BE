package org.devkor.apu.saerok_server.global.shared.util;

public class RegexMatchUtils {

    private RegexMatchUtils() {}

    public static boolean isEmailValid(String email) {
        if (email == null) return false;

        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
}
