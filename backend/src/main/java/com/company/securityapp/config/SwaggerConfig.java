package com.company.securityapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Online Course Security API")
                        .description(
                                "RESTful API demo for an Applied Cryptography project using JWT, bcrypt, AES-GCM, "
                                        + "role-based enrollment approval, HTTPS/TLS, audit logging, and OWASP API Security testing.")
                        .version("1.0.0")
                        .contact(new Contact().name("Applied Cryptography Student Project"))
                        .license(new License().name("Educational Use")))
                .servers(List.of(new Server().url("/").description("Same-origin reverse proxy")))
                .schemaRequirement(
                        "bearerAuth",
                        new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
