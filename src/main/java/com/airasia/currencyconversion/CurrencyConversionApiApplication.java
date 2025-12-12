package com.airasia.currencyconversion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CurrencyConversionApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurrencyConversionApiApplication.class, args);
    }
}
