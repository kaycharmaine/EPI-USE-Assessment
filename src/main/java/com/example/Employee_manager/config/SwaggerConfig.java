package com.example.Employee_manager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI employeeManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Employee Management System API")
                        .description("REST API for managing employee hierarchy and organizational structure. " +
                                "Includes CRUD operations, search, and organizational hierarchy endpoints.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("EPI-USE Africa")
                                .email("support@epiuse.com")));
    }
}