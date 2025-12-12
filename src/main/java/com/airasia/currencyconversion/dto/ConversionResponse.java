package com.airasia.currencyconversion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for currency conversion
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionResponse {
    
    private String from;
    private String to;
    private Double amount;
    private Double convertedAmount;
    private Double exchangeRate;
    private LocalDateTime timestamp;
}
