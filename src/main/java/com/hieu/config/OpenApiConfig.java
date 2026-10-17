package com.hieu.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI indMartOpenAPI() {
        Info info = new Info()
                .title("IND MART API")
                .description("REST API for IND MART fresh vegetable online store")
                .version("1.0.0")
                .contact(new Contact().name("Nguyen Ngoc Hieu").email("hieu@example.com"))
                .license(new License().name("MIT"));

        SecurityScheme bearerScheme = new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Authorization header with Bearer token");

        Components components = new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, bearerScheme);

        return new OpenAPI()
                .info(info)
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(components);
    }
}
