package org.cts.transactionservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures Swagger UI to show an "Authorize" button where you can input
 * the gateway-injected headers (X-User-Id, X-Account-Id, X-Branch-Id, X-Roles).
 *
 * In production these headers are set by the API Gateway after JWT validation.
 * For local testing via Swagger, enter them manually after clicking "Authorize".
 *
 * Example values:
 *   X-User-Id    : 1
 *   X-Account-Id : 101
 *   X-Branch-Id  : 1
 *   X-Roles      : CUSTOMER
 */
@Configuration
public class SwaggerConfig {

    private static final String X_USER_ID    = "X-User-Id";
    private static final String X_ACCOUNT_ID = "X-Account-Id";
    private static final String X_BRANCH_ID  = "X-Branch-Id";
    private static final String X_ROLES      = "X-Roles";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Transaction Service API")
                        .version("1.0.0")
                        .description("""
                                Transaction Microservice API.
                                
                                **For local testing**, click the **Authorize** button and fill in the headers:
                                - `X-User-Id`    — e.g. `1`
                                - `X-Account-Id` — e.g. `101`
                                - `X-Branch-Id`  — e.g. `1`
                                - `X-Roles`      — e.g. `CUSTOMER` or `BRANCH_MANAGER` or `ADMIN`
                                
                                In production these are injected automatically by the API Gateway.
                                """))
                // Register each header as an API-key security scheme
                .components(new Components()
                        .addSecuritySchemes(X_USER_ID, headerScheme(X_USER_ID, "Logged-in user ID (Long)"))
                        .addSecuritySchemes(X_ACCOUNT_ID, headerScheme(X_ACCOUNT_ID, "Logged-in user's account ID (Long)"))
                        .addSecuritySchemes(X_BRANCH_ID, headerScheme(X_BRANCH_ID, "Logged-in user's branch ID (Long)"))
                        .addSecuritySchemes(X_ROLES, headerScheme(X_ROLES, "Comma-separated roles, e.g. CUSTOMER or BRANCH_MANAGER"))
                )
                // Apply all four headers globally to every endpoint
                .addSecurityItem(new SecurityRequirement()
                        .addList(X_USER_ID)
                        .addList(X_ACCOUNT_ID)
                        .addList(X_BRANCH_ID)
                        .addList(X_ROLES)
                );
    }

    private SecurityScheme headerScheme(String headerName, String description) {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name(headerName)
                .description(description);
    }
}

