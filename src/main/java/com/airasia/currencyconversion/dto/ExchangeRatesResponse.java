package com.airasia.currencyconversion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO from Open Exchange Rates API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRatesResponse {
    
    private String disclaimer;
    private String license;
    private Long timestamp;
    private String base;
    
    @JsonProperty("rates")
    private Map<String, Double> rates;
}
