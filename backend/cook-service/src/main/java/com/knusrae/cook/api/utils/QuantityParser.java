package com.knusrae.cook.api.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 수량 문자열을 BigDecimal로 변환하는 유틸리티 클래스
 * 분수 형식(예: "1/2", "3/4")과 소수 형식을 모두 지원합니다.
 */
public class QuantityParser {
    
    /**
     * 수량 문자열을 BigDecimal로 변환
     * 
     * @param quantityString 수량 문자열 (예: "1/2", "3/4", "1.5", "2")
     * @return 변환된 BigDecimal 값, 파싱 실패 시 null
     */
    public static BigDecimal parseQuantity(String quantityString) {
        if (quantityString == null || quantityString.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = quantityString.trim();
        
        // 분수 형식 체크 (예: "1/2", "3/4", "1 1/2")
        if (trimmed.contains("/")) {
            return parseFraction(trimmed);
        }
        
        // 소수 형식 파싱
        try {
            return new BigDecimal(trimmed);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 분수 문자열을 BigDecimal로 변환
     * 
     * @param fractionString 분수 문자열 (예: "1/2", "3/4", "1 1/2")
     * @return 변환된 BigDecimal 값
     */
    private static BigDecimal parseFraction(String fractionString) {
        try {
            String trimmed = fractionString.trim();
            
            // 정수 부분과 분수 부분 분리 (예: "1 1/2" -> 정수: 1, 분수: "1/2")
            BigDecimal wholePart = BigDecimal.ZERO;
            String fractionPart = trimmed;
            
            if (trimmed.contains(" ")) {
                String[] parts = trimmed.split(" ", 2);
                if (parts.length == 2) {
                    try {
                        wholePart = new BigDecimal(parts[0]);
                        fractionPart = parts[1];
                    } catch (NumberFormatException e) {
                        // 정수 부분 파싱 실패 시 전체를 분수로 처리
                        fractionPart = trimmed;
                    }
                }
            }
            
            // 분수 부분 파싱 (예: "1/2" -> 분자: 1, 분모: 2)
            String[] fractionParts = fractionPart.split("/");
            if (fractionParts.length != 2) {
                return null;
            }
            
            BigDecimal numerator = new BigDecimal(fractionParts[0].trim());
            BigDecimal denominator = new BigDecimal(fractionParts[1].trim());
            
            if (denominator.compareTo(BigDecimal.ZERO) == 0) {
                return null; // 0으로 나누기 방지
            }
            
            // 분수 계산: 정수 부분 + (분자 / 분모)
            BigDecimal fraction = numerator.divide(denominator, 10, RoundingMode.HALF_UP);
            return wholePart.add(fraction);
            
        } catch (Exception e) {
            return null;
        }
    }
}

