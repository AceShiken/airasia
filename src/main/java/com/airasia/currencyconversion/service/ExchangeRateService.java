package com.airasia.currencyconversion.service;

import com.airasia.currencyconversion.config.AppConfig;
import com.airasia.currencyconversion.dto.ConversionResponse;
import com.airasia.currencyconversion.dto.ExchangeRatesResponse;
import com.airasia.currencyconversion.exception.CurrencyNotFoundException;
import com.airasia.currencyconversion.exception.ExternalApiException;
import com.airasia.currencyconversion.exception.InvalidConversionRequestException;
import com.airasia.currencyconversion.model.ExchangeRate;
import com.airasia.currencyconversion.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

/**
 * Service for currency conversion and exchange rate operations
 * Implements caching to minimize API calls and comply with rate limits
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateService {
    
    private final RestTemplate restTemplate;
    private final AppConfig appConfig;
    private final ExchangeRateRepository exchangeRateRepository;
    
    /**
     * Fetch latest exchange rates from Open Exchange Rates API
     * Results are cached for 1 hour to minimize API calls
     * 
     * @return Map of currency codes to exchange rates
     */
    @Cacheable(value = "exchangeRates", unless = "#result == null")
    @Transactional
    public Map<String, Double> getLatestRates() {
        log.info("Fetching latest exchange rates from Open Exchange Rates API");
        
        try {
            String url = String.format("%s/latest.json?app_id=%s", 
                appConfig.getBaseUrl(), 
                appConfig.getApiKey());
            
            ExchangeRatesResponse response = restTemplate.getForObject(url, ExchangeRatesResponse.class);
            
            if (response == null || response.getRates() == null) {
                throw new ExternalApiException("Failed to fetch exchange rates from API");
            }
            
            // Store rates in H2 database for persistence
            storeRatesInDatabase(response);
            
            log.info("Successfully fetched {} exchange rates", response.getRates().size());
            return response.getRates();
            
        } catch (RestClientException e) {
            log.error("Error calling Open Exchange Rates API", e);
            
            // Fallback to database if API call fails
            return getRatesFromDatabase();
        }
    }
    
    /**
     * Store exchange rates in H2 database
     */
    @Transactional
    private void storeRatesInDatabase(ExchangeRatesResponse response) {
        LocalDateTime timestamp = LocalDateTime.ofInstant(
            Instant.ofEpochSecond(response.getTimestamp()), 
            ZoneId.systemDefault()
        );
        
        response.getRates().forEach((currency, rate) -> {
            ExchangeRate exchangeRate = exchangeRateRepository
                .findByCurrencyAndBaseCurrency(currency, response.getBase())
                .orElse(new ExchangeRate());
            
            exchangeRate.setCurrency(currency);
            exchangeRate.setRate(rate);
            exchangeRate.setBaseCurrency(response.getBase());
            exchangeRate.setLastUpdated(timestamp);
            
            exchangeRateRepository.save(exchangeRate);
        });
        
        log.info("Stored {} rates in database", response.getRates().size());
    }
    
    /**
     * Fallback method to retrieve rates from database
     */
    private Map<String, Double> getRatesFromDatabase() {
        log.warn("Falling back to database for exchange rates");
        
        try {
            return exchangeRateRepository.findByBaseCurrency("USD")
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                    ExchangeRate::getCurrency,
                    ExchangeRate::getRate
                ));
        } catch (Exception e) {
            throw new ExternalApiException("Failed to fetch exchange rates from API and database", e);
        }
    }
    
    /**
     * Convert amount from one currency to another
     * Since the free plan doesn't support direct conversion endpoint,
     * we fetch all rates and calculate the conversion manually
     * 
     * @param from Source currency code
     * @param to Target currency code
     * @param amount Amount to convert
     * @return ConversionResponse with conversion details
     */
    public ConversionResponse convertCurrency(String from, String to, Double amount) {
        log.info("Converting {} {} to {}", amount, from, to);
        
        // Validation
        validateConversionRequest(from, to, amount);
        
        // Get latest rates (cached)
        Map<String, Double> rates = getLatestRates();
        
        // Check if currencies exist
        if (!rates.containsKey(from.toUpperCase())) {
            throw new CurrencyNotFoundException("Currency not found: " + from);
        }
        if (!rates.containsKey(to.toUpperCase())) {
            throw new CurrencyNotFoundException("Currency not found: " + to);
        }
        
        // Calculate conversion
        // Since rates are based on USD, we need to convert:
        // 1. from -> USD (divide by from rate)
        // 2. USD -> to (multiply by to rate)
        Double fromRate = rates.get(from.toUpperCase());
        Double toRate = rates.get(to.toUpperCase());
        Double exchangeRate = toRate / fromRate;
        Double convertedAmount = amount * exchangeRate;
        
        log.info("Conversion successful: {} {} = {} {}", amount, from, convertedAmount, to);
        
        return ConversionResponse.builder()
            .from(from.toUpperCase())
            .to(to.toUpperCase())
            .amount(amount)
            .convertedAmount(Math.round(convertedAmount * 100.0) / 100.0) // Round to 2 decimal places
            .exchangeRate(Math.round(exchangeRate * 1000000.0) / 1000000.0) // Round to 6 decimal places
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    /**
     * Validate conversion request parameters
     */
    private void validateConversionRequest(String from, String to, Double amount) {
        if (from == null || from.trim().isEmpty()) {
            throw new InvalidConversionRequestException("Source currency cannot be empty");
        }
        if (to == null || to.trim().isEmpty()) {
            throw new InvalidConversionRequestException("Target currency cannot be empty");
        }
        if (amount == null || amount <= 0) {
            throw new InvalidConversionRequestException("Amount must be greater than 0");
        }
        if (from.length() != 3 || to.length() != 3) {
            throw new InvalidConversionRequestException("Currency codes must be 3 characters (ISO 4217)");
        }
    }
}
