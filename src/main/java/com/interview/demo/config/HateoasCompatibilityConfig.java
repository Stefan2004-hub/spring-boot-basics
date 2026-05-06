package com.interview.demo.config;

import org.springframework.boot.autoconfigure.hateoas.HateoasProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class HateoasCompatibilityConfig {

    @Bean
    @Primary
    public HateoasProperties hateoasProperties() {
        HateoasProperties properties = new HateoasProperties();
        // Set default values that would be expected by SpringDoc
        return properties;
    }
}