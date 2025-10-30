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
                registry.addMapping("/**")
                        .allowedOriginPatterns(
                                // Ambiente local
                                "http://localhost:3000",
                                // Front hospedado no Vercel
                                "https://*.vercel.app",
                                // Back hospedado no Render
                                "https://*.onrender.com",
                                // Túnel temporário (opcional, só se ainda usar)
                                "https://*.ngrok-free.app",
                                "https://*.loca.lt"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization")
                        .allowCredentials(true);
            }
        };
    }
}
