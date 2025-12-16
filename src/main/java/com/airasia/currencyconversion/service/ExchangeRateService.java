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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
    private final CacheManager cacheManager;
    
    private static final String CACHE_NAME = "exchangeRates";
    private static final String CACHE_KEY = "latestRates";
    
    /**
     * Fetch latest exchange rates with Cache → API → DB fallback strategy
     * 1. First checks cache
     * 2. If not in cache, calls API and stores in both cache and DB
     * 3. If API fails, falls back to DB
     * 
     * @return Map of currency codes to exchange rates
     */
    @Transactional
    public Map<String, Double> getLatestRates() {
        // Step 1: Check cache first
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            Cache.ValueWrapper cachedValue = cache.get(CACHE_KEY);
            if (cachedValue != null) {
                @SuppressWarnings("unchecked")
                Map<String, Double> rates = (Map<String, Double>) cachedValue.get();
                if (rates != null && !rates.isEmpty()) {
                    log.info("Returning {} exchange rates from cache", rates.size());
                    return rates;
                }
            }
        }
        
        // Step 2: Cache miss - call API
        log.info("Cache miss - fetching latest exchange rates from Open Exchange Rates API");
        
        try {
            String url = "%s/latest.json?app_id=%s".formatted(
                appConfig.getBaseUrl(),
                appConfig.getApiKey());
            
            ExchangeRatesResponse response = restTemplate.getForObject(url, ExchangeRatesResponse.class);
            
            if (response == null || response.getRates() == null) {
                throw new ExternalApiException("Failed to fetch exchange rates from API");
            }
            
            Map<String, Double> rates = response.getRates();
            
            // Store rates in database for persistence
            storeRatesInDatabase(response);
            
            // Store in cache
            if (cache != null) {
                cache.put(CACHE_KEY, rates);
                log.info("Stored {} rates in cache", rates.size());
            }
            
            log.info("Successfully fetched {} exchange rates from API", rates.size());
            return rates;
            
        } catch (RestClientException e) {
            log.error("Error calling Open Exchange Rates API: {}", e.getMessage());
            
            // Step 3: API failed - fallback to database
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
     * Fallback method to retrieve rates from database when API is unavailable
     */
    private Map<String, Double> getRatesFromDatabase() {
        log.warn("API unavailable - falling back to database for exchange rates");
        
        try {
            Map<String, Double> rates = exchangeRateRepository.findByBaseCurrency("USD")
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                    ExchangeRate::getCurrency,
                    ExchangeRate::getRate
                ));
            
            if (rates.isEmpty()) {
                throw new ExternalApiException(
                    "No exchange rates available: API is down and database is empty. " +
                    "Please try again later or ensure the API is accessible."
                );
            }
            
            log.info("Retrieved {} exchange rates from database", rates.size());
            return rates;
            
        } catch (Exception e) {
            if (e instanceof ExternalApiException) {
                throw e;
            }
            throw new ExternalApiException(
                "Failed to fetch exchange rates from both API and database", e
            );
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
