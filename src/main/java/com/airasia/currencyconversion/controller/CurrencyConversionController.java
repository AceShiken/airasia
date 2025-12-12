package com.airasia.currencyconversion.controller;

import com.airasia.currencyconversion.dto.ConversionResponse;
import com.airasia.currencyconversion.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for currency conversion operations
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class CurrencyConversionController {
    
    private final ExchangeRateService exchangeRateService;
    
    /**
     * Convert currency from one to another
     * 
     * @param from Source currency code (e.g., USD)
     * @param to Target currency code (e.g., EUR)
     * @param amount Amount to convert
     * @return ConversionResponse with conversion details
     */
    @GetMapping("/convert")
    public ResponseEntity<ConversionResponse> convertCurrency(
            @RequestParam(name = "from") String from,
            @RequestParam(name = "to") String to,
            @RequestParam(name = "amount") Double amount) {
        
        log.info("Received conversion request: {} {} to {}", amount, from, to);
        
        ConversionResponse response = exchangeRateService.convertCurrency(from, to, amount);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all latest exchange rates
     * Bonus endpoint
     * 
     * @return Map of currency codes to exchange rates
     */
    @GetMapping("/rates")
    public ResponseEntity<Map<String, Double>> getLatestRates() {
        log.info("Received request for latest exchange rates");
        
        Map<String, Double> rates = exchangeRateService.getLatestRates();
        
        return ResponseEntity.ok(rates);
    }
    
    /**
     * Health check endpoint
     * 
     * @return Simple status message
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Currency Conversion API",
            "version", "1.0.0"
        ));
    }
}
