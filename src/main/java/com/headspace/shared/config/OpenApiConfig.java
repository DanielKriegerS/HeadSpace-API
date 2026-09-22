package com.headspace.shared.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "HeadSpace API", version = "0.1.0-stage-0", description = "Etapa 0 do backend HeadSpace"),
        security = {
                @SecurityRequirement(name = "sessionCookie"),
                @SecurityRequirement(name = "csrfToken")
        },
        tags = {
                @Tag(name = "Identity", description = "Identidade e sessão do usuário")
        }
)
@SecurityScheme(name = "sessionCookie", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.COOKIE, paramName = "HEADSPACE_SESSION")
@SecurityScheme(name = "csrfToken", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = "X-XSRF-TOKEN")
public class OpenApiConfig {
}
