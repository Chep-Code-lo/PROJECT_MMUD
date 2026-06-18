package com.company.securityapp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI securityAppOpenApi(
            @Value("${app.api.base-url}") String baseUrl,
            @Value("${spring.application.name}") String appName) {
        return new OpenAPI()
                .info(new Info()
                        .title("Security App API")
                        .version("v1")
                        .description("Backend foundation for the applied cryptography project.")
                        .contact(new Contact().name("MMUD Team")))
                .servers(List.of(new Server().url(baseUrl).description(appName + " local server")))
                .components(new Components().addSecuritySchemes(
                        "bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}

