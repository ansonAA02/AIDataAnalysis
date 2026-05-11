package com.aifinance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@ConfigurationPropertiesScan
// Enable Spring caching to significantly reduce LLM API latency
@EnableCaching
public class AiFinanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiFinanceApplication.class, args);
    }
}
