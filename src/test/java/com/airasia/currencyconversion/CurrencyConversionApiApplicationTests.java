package com.airasia.currencyconversion;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration test for the application context
 */
@SpringBootTest
@TestPropertySource(properties = {
    "openexchangerates.api.key=test-key"
})
class CurrencyConversionApiApplicationTests {
    
    @Test
    void contextLoads() {
        // This test ensures that the Spring application context loads successfully
    }
}
