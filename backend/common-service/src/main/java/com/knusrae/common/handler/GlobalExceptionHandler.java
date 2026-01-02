package com.knusrae.common.handler;

import com.knusrae.common.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * 전역 예외 처리 핸들러
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 잘못된 요청 (IllegalArgumentException)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("IllegalArgumentException: {}", e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                "BAD_REQUEST",
                e.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Validation 실패 (MethodArgumentNotValidException)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("Validation failed: {}", e.getMessage());
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("입력값 검증에 실패했습니다.");
        ErrorResponse errorResponse = new ErrorResponse(
                "VALIDATION_ERROR",
                errorMessage,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 접근 거부 (AccessDeniedException)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("AccessDeniedException: {}", e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                "FORBIDDEN",
                e.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * 인증되지 않은 요청
     */
    @ExceptionHandler({
            org.springframework.security.core.AuthenticationException.class,
            org.springframework.security.authentication.BadCredentialsException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthenticationException(Exception e) {
        log.warn("AuthenticationException: {}", e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                "UNAUTHORIZED",
                e.getMessage() != null ? e.getMessage() : "인증이 필요합니다.",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * 리소스를 찾을 수 없음
     */
    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
            jakarta.persistence.EntityNotFoundException e) {
        log.warn("EntityNotFoundException: {}", e.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                "NOT_FOUND",
                e.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 기타 예상치 못한 예외
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected exception occurred", e);
        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "서버 내부 오류가 발생했습니다.",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

