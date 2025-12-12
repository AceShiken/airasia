package com.airasia.currencyconversion.controller;

import com.airasia.currencyconversion.dto.ConversionResponse;
import com.airasia.currencyconversion.exception.CurrencyNotFoundException;
import com.airasia.currencyconversion.exception.InvalidConversionRequestException;
import com.airasia.currencyconversion.service.ExchangeRateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for CurrencyConversionController
 */
@WebMvcTest(CurrencyConversionController.class)
class CurrencyConversionControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ExchangeRateService exchangeRateService;
    
    @Test
    void testConvertCurrency_Success() throws Exception {
        // Arrange
        ConversionResponse mockResponse = ConversionResponse.builder()
            .from("USD")
            .to("EUR")
            .amount(100.0)
            .convertedAmount(85.0)
            .exchangeRate(0.85)
            .timestamp(LocalDateTime.now())
            .build();
        
        when(exchangeRateService.convertCurrency("USD", "EUR", 100.0))
            .thenReturn(mockResponse);
        
        // Act & Assert
        mockMvc.perform(get("/convert")
                .param("from", "USD")
                .param("to", "EUR")
                .param("amount", "100.0")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.from").value("USD"))
            .andExpect(jsonPath("$.to").value("EUR"))
            .andExpect(jsonPath("$.amount").value(100.0))
            .andExpect(jsonPath("$.convertedAmount").value(85.0))
            .andExpect(jsonPath("$.exchangeRate").value(0.85));
    }
    
    @Test
    void testConvertCurrency_MissingParameters() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/convert")
                .param("from", "USD")
                .param("to", "EUR")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void testConvertCurrency_InvalidAmount() throws Exception {
        // Arrange
        when(exchangeRateService.convertCurrency(anyString(), anyString(), anyDouble()))
            .thenThrow(new InvalidConversionRequestException("Amount must be greater than 0"));
        
        // Act & Assert
        mockMvc.perform(get("/convert")
                .param("from", "USD")
                .param("to", "EUR")
                .param("amount", "-100.0")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Amount must be greater than 0"));
    }
    
    @Test
    void testConvertCurrency_CurrencyNotFound() throws Exception {
        // Arrange
        when(exchangeRateService.convertCurrency(anyString(), anyString(), anyDouble()))
            .thenThrow(new CurrencyNotFoundException("Currency not found: XYZ"));
        
        // Act & Assert
        mockMvc.perform(get("/convert")
                .param("from", "USD")
                .param("to", "XYZ")
                .param("amount", "100.0")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Currency not found: XYZ"));
    }
    
    @Test
    void testGetLatestRates_Success() throws Exception {
        // Arrange
        Map<String, Double> mockRates = new HashMap<>();
        mockRates.put("USD", 1.0);
        mockRates.put("EUR", 0.85);
        mockRates.put("GBP", 0.73);
        
        when(exchangeRateService.getLatestRates()).thenReturn(mockRates);
        
        // Act & Assert
        mockMvc.perform(get("/rates")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.USD").value(1.0))
            .andExpect(jsonPath("$.EUR").value(0.85))
            .andExpect(jsonPath("$.GBP").value(0.73));
    }
    
    @Test
    void testHealthCheck() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.service").value("Currency Conversion API"));
    }
}
