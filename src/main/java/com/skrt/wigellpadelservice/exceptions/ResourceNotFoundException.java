package com.skrt.wigellpadelservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private final String resource;
    private final Object id;


    public ResourceNotFoundException(String resource, Object id) {
        super("%s not found: %s".formatted(resource, id));
        this.resource = resource;
        this.id = id;
    }

    public String getResource() {
        return resource;
    }
    public Object getId() {
        return id;
    }
}
