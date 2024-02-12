package com.enrich.authn;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration	
@EnableSwagger2
public class SwaggerConfiguration implements WebMvcConfigurer{

 @Bean
 @Lazy
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
         .select()
                 .apis(RequestHandlerSelectors.basePackage("com.enrich.authn"))
                 .paths(PathSelectors.any())
                 .build()
                 .apiInfo(apiInfo());
    }	

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Enrich-authn-services")
                .description("API Documentation for Enrich-authn-services")
                .license("Apache 2.0")
                .licenseUrl("https://www.apache.org./licenses/LICENSE-2.0\"")
                .version("1.0.0")
                .build();
    }
 
}
