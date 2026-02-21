package com.knusrae.common.exception;

/**
 * 요청한 리소스를 찾을 수 없을 때 사용하는 예외.
 * GlobalExceptionHandler에서 404 NOT_FOUND로 매핑된다.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
