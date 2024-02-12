package com.enrich.authn.provider;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("*") // Allow requests from any origin
            .allowedMethods("GET", "POST", "PUT", "DELETE") // Allow specified HTTP methods
            .allowedHeaders("*") // Allow all headers
            .allowCredentials(false) // Allow credentials like cookies
            .maxAge(3600); // Max age of pre-flight requests in seconds
    }
}
