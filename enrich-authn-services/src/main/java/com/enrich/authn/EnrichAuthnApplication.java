package com.enrich.authn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;



@SpringBootApplication
@EnableCaching
public class EnrichAuthnApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnrichAuthnApplication.class, args);
		System.getProperty("java.version") ;
	}
}

