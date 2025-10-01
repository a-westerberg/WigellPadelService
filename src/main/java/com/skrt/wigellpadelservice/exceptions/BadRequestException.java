package com.skrt.wigellpadelservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    private final String field;
    private final Object value;
    private final String reason;

    public BadRequestException(String field, String reason) {
        this(field, reason, null);
    }

    public BadRequestException(String field, String reason, Object value) {
        super(buildMessage(field,reason,value));
        this.field = field;
        this.reason = reason;
        this.value = value;
    }


    private static String buildMessage(String field, String reason, Object value) {
        return value == null ? "%s: %s".formatted(field, reason)
                : "%s: %s (value=%s)".formatted(field, reason, value);
    }

    public String getField() {
        return field;
    }
    public Object getValue() {
        return value;
    }
    public String getReason() {
        return reason;
    }
}
