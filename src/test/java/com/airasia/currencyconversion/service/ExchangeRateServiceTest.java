package com.airasia.currencyconversion.service;

import com.airasia.currencyconversion.config.AppConfig;
import com.airasia.currencyconversion.dto.ConversionResponse;
import com.airasia.currencyconversion.dto.ExchangeRatesResponse;
import com.airasia.currencyconversion.exception.CurrencyNotFoundException;
import com.airasia.currencyconversion.exception.ExternalApiException;
import com.airasia.currencyconversion.exception.InvalidConversionRequestException;
import com.airasia.currencyconversion.model.ExchangeRate;
import com.airasia.currencyconversion.repository.ExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExchangeRateService
 */
@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {
    
    @Mock
    private RestTemplate restTemplate;
    
    @Mock
    private AppConfig appConfig;
    
    @Mock
    private ExchangeRateRepository exchangeRateRepository;
    
    @Mock
    private CacheManager cacheManager;
    
    @Mock
    private Cache cache;
    
    @InjectMocks
    private ExchangeRateService exchangeRateService;
    
    private ExchangeRatesResponse mockResponse;
    private Map<String, Double> mockRates;
    
    @BeforeEach
    void setUp() {
        mockRates = new HashMap<>();
        mockRates.put("USD", 1.0);
        mockRates.put("EUR", 0.85);
        mockRates.put("GBP", 0.73);
        mockRates.put("JPY", 110.0);
        
        mockResponse = new ExchangeRatesResponse();
        mockResponse.setBase("USD");
        mockResponse.setTimestamp(System.currentTimeMillis() / 1000);
        mockResponse.setRates(mockRates);
        
        lenient().when(appConfig.getApiKey()).thenReturn("test-api-key");
        lenient().when(appConfig.getBaseUrl()).thenReturn("https://openexchangerates.org/api");
        
        // Mock cache behavior - return null to simulate cache miss by default
        lenient().when(cacheManager.getCache("exchangeRates")).thenReturn(cache);
        lenient().when(cache.get("latestRates")).thenReturn(null);
    }
    
    @Test
    void testGetLatestRates_Success() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(ExchangeRatesResponse.class)))
            .thenReturn(mockResponse);
        when(exchangeRateRepository.findByCurrencyAndBaseCurrency(anyString(), anyString()))
            .thenReturn(Optional.empty());
        
        // Act
        Map<String, Double> rates = exchangeRateService.getLatestRates();
        
        // Assert
        assertNotNull(rates);
        assertEquals(4, rates.size());
        assertEquals(1.0, rates.get("USD"));
        assertEquals(0.85, rates.get("EUR"));
        verify(restTemplate, times(1)).getForObject(anyString(), eq(ExchangeRatesResponse.class));
    }
    
    @Test
    void testGetLatestRates_ApiFailure_FallbackToDatabase() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(ExchangeRatesResponse.class)))
            .thenThrow(new RestClientException("API Error"));
        
        List<ExchangeRate> dbRates = Arrays.asList(
            new ExchangeRate(1L, "EUR", 0.85, LocalDateTime.now(), "USD"),
            new ExchangeRate(2L, "GBP", 0.73, LocalDateTime.now(), "USD")
        );
        when(exchangeRateRepository.findByBaseCurrency("USD")).thenReturn(dbRates);
        
        // Act
        Map<String, Double> rates = exchangeRateService.getLatestRates();
        
        // Assert
        assertNotNull(rates);
        assertEquals(2, rates.size());
        assertEquals(0.85, rates.get("EUR"));
        verify(exchangeRateRepository, times(1)).findByBaseCurrency("USD");
    }
    
    @Test
    void testConvertCurrency_Success() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(ExchangeRatesResponse.class)))
            .thenReturn(mockResponse);
        when(exchangeRateRepository.findByCurrencyAndBaseCurrency(anyString(), anyString()))
            .thenReturn(Optional.empty());
        
        // Act
        ConversionResponse response = exchangeRateService.convertCurrency("USD", "EUR", 100.0);
        
        // Assert
        assertNotNull(response);
        assertEquals("USD", response.getFrom());
        assertEquals("EUR", response.getTo());
        assertEquals(100.0, response.getAmount());
        assertEquals(85.0, response.getConvertedAmount());
        assertNotNull(response.getTimestamp());
    }
    
    @Test
    void testConvertCurrency_CrossConversion() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(ExchangeRatesResponse.class)))
            .thenReturn(mockResponse);
        when(exchangeRateRepository.findByCurrencyAndBaseCurrency(anyString(), anyString()))
            .thenReturn(Optional.empty());
        
        // Act - Convert EUR to GBP
        ConversionResponse response = exchangeRateService.convertCurrency("EUR", "GBP", 100.0);
        
        // Assert
        assertNotNull(response);
        assertEquals("EUR", response.getFrom());
        assertEquals("GBP", response.getTo());
        assertEquals(100.0, response.getAmount());
        // EUR to GBP: (100 / 0.85) * 0.73 = 85.88
        assertTrue(response.getConvertedAmount() > 85 && response.getConvertedAmount() < 86);
    }
    
    @Test
    void testConvertCurrency_InvalidAmount() {
        // Act & Assert
        assertThrows(InvalidConversionRequestException.class, () -> {
            exchangeRateService.convertCurrency("USD", "EUR", -100.0);
        });
    }
    
    @Test
    void testConvertCurrency_NullAmount() {
        // Act & Assert
        assertThrows(InvalidConversionRequestException.class, () -> {
            exchangeRateService.convertCurrency("USD", "EUR", null);
        });
    }
    
    @Test
    void testConvertCurrency_EmptyFromCurrency() {
        // Act & Assert
        assertThrows(InvalidConversionRequestException.class, () -> {
            exchangeRateService.convertCurrency("", "EUR", 100.0);
        });
    }
    
    @Test
    void testConvertCurrency_InvalidCurrencyCode() {
        // Act & Assert
        assertThrows(InvalidConversionRequestException.class, () -> {
            exchangeRateService.convertCurrency("US", "EUR", 100.0);
        });
    }
    
    @Test
    void testConvertCurrency_CurrencyNotFound() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(ExchangeRatesResponse.class)))
            .thenReturn(mockResponse);
        when(exchangeRateRepository.findByCurrencyAndBaseCurrency(anyString(), anyString()))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(CurrencyNotFoundException.class, () -> {
            exchangeRateService.convertCurrency("USD", "XYZ", 100.0);
        });
    }
}
