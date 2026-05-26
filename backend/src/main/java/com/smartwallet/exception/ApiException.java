package com.smartwallet.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String detail;

    public ApiException(HttpStatus status, String detail) {
        super(detail);
        this.status = status;
        this.detail = detail;
    }
}
