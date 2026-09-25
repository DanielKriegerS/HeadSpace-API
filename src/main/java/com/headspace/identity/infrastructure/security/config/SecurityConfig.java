package com.headspace.identity.infrastructure.security.config;

import com.headspace.identity.infrastructure.security.handler.ApiLogoutSuccessHandler;
import com.headspace.identity.infrastructure.security.properties.SecurityProperties;
import com.headspace.shared.error.RestAccessDeniedHandler;
import com.headspace.shared.error.RestAuthenticationEntryPoint;
import com.headspace.shared.web.CorrelationIdFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    private final SecurityProperties securityProperties;
    private final OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService;
    private final ApiLogoutSuccessHandler logoutSuccessHandler;

    public SecurityConfig(
            SecurityProperties securityProperties,
            OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService,
            ApiLogoutSuccessHandler logoutSuccessHandler
    ) {
        this.securityProperties = securityProperties;
        this.oidcUserService = oidcUserService;
        this.logoutSuccessHandler = logoutSuccessHandler;
    }

    @Bean
    public CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository =
                CookieCsrfTokenRepository.withHttpOnlyFalse();

        repository.setCookieName(
                securityProperties.csrfCookieName()
        );

        repository.setHeaderName(
                securityProperties.csrfHeaderName()
        );

        repository.setCookiePath("/");

        return repository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorrelationIdFilter correlationIdFilter,
            CookieCsrfTokenRepository csrfTokenRepository,
            CorsConfigurationSource corsConfigurationSource,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
                .addFilterBefore(
                        correlationIdFilter,
                        SecurityContextHolderFilter.class
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                )
                .cors(cors -> cors
                        .configurationSource(corsConfigurationSource)
                )
                .sessionManagement(session -> session
                        .sessionFixation()
                        .migrateSession()
                        .sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/error",
                                "/actuator/health",
                                "/actuator/info",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/oauth2/authorization/**",
                                "/login/oauth2/code/**",
                                "/api/v1/auth/csrf"
                        )
                        .permitAll()
                        .requestMatchers(
                                "/api/v1/me",
                                "/api/v1/auth/logout"
                        )
                        .authenticated()
                        .anyRequest()
                        .denyAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(oidcUserService)
                        )
                        .defaultSuccessUrl(
                                securityProperties.frontendBaseUrl(),
                                true
                        )
                        .failureUrl("/error?oauth2=failed")
                )
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .logoutSuccessHandler(logoutSuccessHandler)
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies(
                                securityProperties.sessionCookieName(),
                                securityProperties.csrfCookieName()
                        )
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(
                                authenticationEntryPoint
                        )
                        .accessDeniedHandler(
                                accessDeniedHandler
                        )
                );

        return http.build();
    }
}