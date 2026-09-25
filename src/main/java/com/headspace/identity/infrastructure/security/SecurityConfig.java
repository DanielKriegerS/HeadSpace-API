package com.headspace.identity.infrastructure.security;

import com.headspace.identity.application.port.AuthenticatedUserProvider;
import com.headspace.identity.application.usecase.GoogleUserSyncUseCase;
import com.headspace.identity.domain.model.User;
import com.headspace.identity.domain.model.UserStatus;
import com.headspace.shared.error.RestAccessDeniedHandler;
import com.headspace.shared.error.RestAuthenticationEntryPoint;
import com.headspace.shared.web.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${headspace.security.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    @Value("${headspace.security.frontend-base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    @Value("${headspace.security.session-cookie-name:HEADSPACE_SESSION}")
    private String sessionCookieName;

    @Value("${headspace.security.csrf-cookie-name:XSRF-TOKEN}")
    private String csrfCookieName;

    @Value("${headspace.security.csrf-header-name:X-XSRF-TOKEN}")
    private String csrfHeaderName;

    @Bean
    public CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository =
                CookieCsrfTokenRepository.withHttpOnlyFalse();

        repository.setCookieName(csrfCookieName);
        repository.setHeaderName(csrfHeaderName);
        repository.setCookiePath("/");
        return repository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorrelationIdFilter correlationIdFilter,
            GoogleUserSyncUseCase googleUserSyncUseCase,
            CookieCsrfTokenRepository csrfTokenRepository,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler
    )  throws Exception {
        http
            .addFilterBefore(correlationIdFilter, SecurityContextHolderFilter.class)
            .csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository))
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionFixation().migrateSession().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/error",
                                            "/actuator/health",
                                            "/actuator/info",
                                            "/v3/api-docs/**",
                                            "/swagger-ui/**",
                                            "/oauth2/authorization/**",
                                            "/login/oauth2/code/**",
                                            "/api/v1/auth/csrf").permitAll()
                .requestMatchers("/api/v1/me").authenticated()
                .requestMatchers("/api/v1/auth/logout").authenticated()
                .anyRequest().denyAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService(googleUserSyncUseCase)))
                .defaultSuccessUrl(frontendBaseUrl, true)
                .failureUrl("/error?oauth2=failed")
            )
            .logout(logout -> logout
                .logoutUrl("/api/v1/auth/logout")
                .logoutSuccessHandler(logoutSuccessHandler())
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies(sessionCookieName, csrfCookieName)
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim).filter(value -> !value.isBlank()).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-XSRF-TOKEN", "X-Correlation-Id"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService(GoogleUserSyncUseCase googleUserSyncUseCase) {
        OidcUserService delegate = new OidcUserService();
        return userRequest -> {
            OidcUser oidcUser = delegate.loadUser(userRequest);
            User user = googleUserSyncUseCase.sync(oidcUser);
            return new HeadSpaceUserPrincipal(user, oidcUser.getAttributes(), oidcUser.getIdToken());
        };
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) ->
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Bean
    public AuthenticatedUserProvider authenticatedUserProvider() {
        return () -> {
            Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
                return Optional.empty();
            }
            Object principal = authentication.getPrincipal();
            if (principal instanceof HeadSpaceUserPrincipal headSpaceUserPrincipal) {
                User user = headSpaceUserPrincipal.getUser();
                if (user.getStatus() == UserStatus.BANNED) {
                    throw new AuthenticationCredentialsNotFoundException("User is banned");
                }
                return Optional.of(user);
            }
            return Optional.empty();
        };
    }
}
