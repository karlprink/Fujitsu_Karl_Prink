package com.fujitsu.delivery.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI (Swagger) documentation.
 * Sets up the API metadata such as title, description, and contact info.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fujitsu Trial Task API (Karl Prink)")
                        .version("1.0.0")
                        .description("REST API for calculating delivery fees based on dynamic regional rules, vehicle types, and real-time weather conditions.")
                        .contact(new Contact()
                                .name("Karl Prink")
                                .email("karlprink58@gmail.com")));
    }
}