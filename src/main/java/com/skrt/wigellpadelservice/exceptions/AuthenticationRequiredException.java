package com.skrt.wigellpadelservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthenticationRequiredException extends RuntimeException {

    private final String action;
    private final String details;

    public AuthenticationRequiredException(String action, String details) {
        super("Authentication required to %s (details=%s)".formatted(action, details));
        this.action = action;
        this.details = details;
    }

    public String getAction() {
        return action;
    }
    public String getDetails() {
        return details;
    }
}
