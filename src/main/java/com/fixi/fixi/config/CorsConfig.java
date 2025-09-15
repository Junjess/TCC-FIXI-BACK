package com.fixi.fixi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // aplica em todos os endpoints
                        .allowedOrigins("http://localhost:3000") // libera o front local
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // libera métodos HTTP
                        .allowedHeaders("*"); // permite todos os headers
            }
        };
    }
}
