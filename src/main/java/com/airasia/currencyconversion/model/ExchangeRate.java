package com.airasia.currencyconversion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing cached exchange rates in H2 database
 */
@Entity
@Table(name = "exchange_rates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 3)
    private String currency;
    
    @Column(nullable = false)
    private Double rate;
    
    @Column(nullable = false)
    private LocalDateTime lastUpdated;
    
    @Column(nullable = false, length = 3)
    private String baseCurrency;
    
    public ExchangeRate(String currency, Double rate, LocalDateTime lastUpdated, String baseCurrency) {
        this.currency = currency;
        this.rate = rate;
        this.lastUpdated = lastUpdated;
        this.baseCurrency = baseCurrency;
    }
}
