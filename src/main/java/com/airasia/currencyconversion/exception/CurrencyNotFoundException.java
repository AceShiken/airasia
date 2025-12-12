package com.airasia.currencyconversion.exception;

/**
 * Exception thrown when currency is not found or invalid
 */
public class CurrencyNotFoundException extends RuntimeException {
    
    public CurrencyNotFoundException(String message) {
        super(message);
    }
}
