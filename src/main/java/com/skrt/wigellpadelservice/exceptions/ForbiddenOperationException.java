package com.skrt.wigellpadelservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenOperationException extends RuntimeException {

    private final String action;
    private final String resourceType;
    private final String resourceId;

    public ForbiddenOperationException(String action, String resourceType, String resourceId) {
        super("Forbidden: cannot %s on %s (id=%s)".formatted(action, resourceType, resourceId));
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getAction() {
        return action;
    }
    public String getResourceType() {
        return resourceType;
    }
    public String getResourceId() {
        return resourceId;
    }
}
