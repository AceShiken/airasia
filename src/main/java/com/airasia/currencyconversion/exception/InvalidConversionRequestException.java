package com.airasia.currencyconversion.exception;

/**
 * Exception thrown for invalid conversion requests
 */
public class InvalidConversionRequestException extends RuntimeException {
    
    public InvalidConversionRequestException(String message) {
        super(message);
    }
}
