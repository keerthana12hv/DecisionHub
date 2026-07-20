package com.decisionhub.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName1 = "BearerAuth";
        final String securitySchemeName2 = "bearerAuth";
        
        return new OpenAPI()
            .info(new Info()
                .title("DecisionHub API")
                .version("1.0.0")
                .description("Collaborative Decision-Making & Community Polling Platform Backend"))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName1))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName2))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName1, new SecurityScheme()
                    .name(securitySchemeName1)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT"))
                .addSecuritySchemes(securitySchemeName2, new SecurityScheme()
                    .name(securitySchemeName2)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
