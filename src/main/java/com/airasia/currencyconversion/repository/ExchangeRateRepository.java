package com.airasia.currencyconversion.repository;

import com.airasia.currencyconversion.model.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ExchangeRate entity
 */
@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    
    Optional<ExchangeRate> findByCurrencyAndBaseCurrency(String currency, String baseCurrency);
    
    List<ExchangeRate> findByBaseCurrency(String baseCurrency);
    
    void deleteByBaseCurrency(String baseCurrency);
}
