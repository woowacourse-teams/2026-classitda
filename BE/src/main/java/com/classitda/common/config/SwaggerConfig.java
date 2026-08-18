package com.classitda.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.customizers.OpenApiCustomizer;

@Configuration
public class SwaggerConfig {

    private static final String API_VERSION_HEADER = "X-API-Version";
    private static final String DEFAULT_API_VERSION = "1";

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("Classitda API")
                .description("Classitda API 명세서")
                .version("1.0.0");

        return new OpenAPI()
                .info(info)
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    public OpenApiCustomizer apiVersionHeaderDefaultCustomizer() {
        return openApi -> openApi.getPaths().values().stream()
                .flatMap(pathItem -> pathItem.readOperations().stream())
                .filter(operation -> operation.getParameters() != null)
                .flatMap(operation -> operation.getParameters().stream())
                .filter(parameter -> API_VERSION_HEADER.equals(parameter.getName()))
                .filter(parameter -> "header".equals(parameter.getIn()))
                .filter(parameter -> parameter.getSchema() != null)
                .forEach(parameter -> parameter.getSchema().setDefault(DEFAULT_API_VERSION));
    }
}
