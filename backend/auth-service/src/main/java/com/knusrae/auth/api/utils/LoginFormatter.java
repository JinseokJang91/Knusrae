package com.knusrae.auth.api.utils;

public class LoginFormatter {
    public static String formatPhoneNumber(String phone) {
        if (phone == null || phone.isBlank()) {
            return phone;
        }

        return phone
                .replaceFirst("^\\+82\\s*", "0") // +82 → 0
                .replaceAll("[^0-9]", "");       // 숫자만 남기기
    }

    public static String formatBirth(String birthyear, String birthday) {
        if (birthyear == null || birthday == null || birthday.isBlank()) {
            return null;
        }

        String monthDay = birthday.replaceAll("[^0-9]", "");

        return birthyear + monthDay;
    }
}
