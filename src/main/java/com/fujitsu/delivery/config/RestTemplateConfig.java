package com.fujitsu.delivery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for providing HTTP client beans.
 */

@Configuration
public class RestTemplateConfig {
    /**
     * Creates and configures a RestTemplate bean for making synchronous HTTP requests.
     *
     * @return a new RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
