package com.airasia.currencyconversion.exception;

/**
 * Exception thrown when external API call fails
 */
public class ExternalApiException extends RuntimeException {
    
    public ExternalApiException(String message) {
        super(message);
    }
    
    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
