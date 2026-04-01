package com.knusrae.common.utils;

/**
 * API 응답·로그 등에서 개인정보 노출을 줄이기 위한 마스킹 유틸.
 * 이메일·전화번호를 일부만 노출하고 나머지는 *** 로 치환합니다.
 */
public final class PiiMaskUtils {

    private PiiMaskUtils() {
    }

    /**
     * 이메일 마스킹 (앞 2자 + *** + @ + 도메인)
     * 예: ab***@example.com
     */
    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }
        int at = email.indexOf('@');
        if (at <= 0 || at >= email.length() - 1) {
            return "***";
        }
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 2) {
            return local + "***" + domain;
        }
        return local.substring(0, 2) + "***" + domain;
    }

    /**
     * 전화번호 마스킹 (앞 3자 + **** + 뒤 4자)
     * 예: 010-****-5678
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return phone;
        }
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() < 4) {
            return "***";
        }
        if (digits.length() <= 8) {
            return digits.substring(0, 3) + "-****-" + digits.substring(digits.length() - 4);
        }
        return digits.substring(0, 3) + "-****-" + digits.substring(digits.length() - 4);
    }
}
