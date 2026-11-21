package cl.duoc.fullstack.ep3.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {

    @Bean
    public OpenAPI ep3OpenApi() {
        return new OpenAPI().info(new Info()
                .title("EP3 — Tienda de herramientas")
                .version("v1")
                .description("""
                        API REST de la Evaluación Parcial 3 de DSY1104 Desarrollo Full Stack II.
                        Autenticación con JWT y autorización por roles ADMIN, VENDEDOR y CLIENTE.
                        """));
    }
}
