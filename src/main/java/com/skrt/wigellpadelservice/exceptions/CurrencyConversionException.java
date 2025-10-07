package com.skrt.wigellpadelservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class CurrencyConversionException extends RuntimeException {

    private final String provider;
    private final String endpoint;
    private final Integer statusCode;
    private final String responseBody;




    public CurrencyConversionException(String provider, String endpoint, String message) {
        super("Currency conversion failed via %s %s: %s".formatted(provider, endpoint, message));
        this.provider = provider;
        this.endpoint = endpoint;
        this.statusCode = null;
        this.responseBody = null;
    }

    public CurrencyConversionException(String provider, String endpoint,Integer statusCode, String responseBody, String message) {
        super("Currency conversion failed via %s %s: %s (status=%s, body=%s)".formatted(provider, endpoint, message, statusCode, responseBody));
        this.provider = provider;
        this.endpoint = endpoint;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public String getProvider() {
        return provider;
    }
    public String getEndpoint() {
        return endpoint;
    }
    public Integer getStatusCode() {
        return statusCode;
    }
    public String getResponseBody() {
        return responseBody;
    }
}
