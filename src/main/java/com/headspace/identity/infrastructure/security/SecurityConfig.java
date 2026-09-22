package com.headspace.identity.infrastructure.security;

import com.headspace.identity.application.port.AuthenticatedUserProvider;
import com.headspace.identity.application.usecase.GoogleUserSyncUseCase;
import com.headspace.identity.domain.model.User;
import com.headspace.identity.domain.model.UserStatus;
import com.headspace.shared.web.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
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

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorrelationIdFilter correlationIdFilter,
            GoogleUserSyncUseCase googleUserSyncUseCase
    ) throws Exception {
        http
            .addFilterBefore(correlationIdFilter, org.springframework.security.web.context.SecurityContextHolderFilter.class)
            .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionFixation().migrateSession().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/error").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
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
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/problem+json");
                    response.setHeader("X-Correlation-Id", CorrelationIdFilter.resolveCorrelationId((HttpServletRequest) request));
                    response.getWriter().write("{\"type\":\"https://headspace.app/problems/unauthorized\",\"title\":\"Authentication required\",\"status\":401,\"detail\":\"Authentication is required to access this resource.\",\"instance\":\"" + request.getRequestURI() + "\",\"code\":\"AUTHENTICATION_REQUIRED\",\"timestamp\":\"" + java.time.Instant.now() + "\",\"correlationId\":\"" + org.slf4j.MDC.get("correlationId") + "\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/problem+json");
                    response.setHeader("X-Correlation-Id", CorrelationIdFilter.resolveCorrelationId((HttpServletRequest) request));
                    response.getWriter().write("{\"type\":\"https://headspace.app/problems/access-denied\",\"title\":\"Access denied\",\"status\":403,\"detail\":\"You do not have permission to access this resource.\",\"instance\":\"" + request.getRequestURI() + "\",\"code\":\"ACCESS_DENIED\",\"timestamp\":\"" + java.time.Instant.now() + "\",\"correlationId\":\"" + org.slf4j.MDC.get("correlationId") + "\"}");
                })
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
    public org.springframework.security.oauth2.client.userinfo.OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService(GoogleUserSyncUseCase googleUserSyncUseCase) {
        OidcUserService delegate = new OidcUserService();
        return userRequest -> {
            OidcUser oidcUser = delegate.loadUser(userRequest);
            User user = googleUserSyncUseCase.sync(oidcUser);
            return new HeadSpaceUserPrincipal(user, oidcUser.getAttributes(), oidcUser.getIdToken());
        };
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            response.setStatus(HttpStatus.NO_CONTENT.value());
            response.addHeader("Set-Cookie", "HEADSPACE_SESSION=; Max-Age=0; Path=/; HttpOnly; SameSite=Lax; Secure=false");
            response.addHeader("Set-Cookie", "XSRF-TOKEN=; Max-Age=0; Path=/; HttpOnly=false; SameSite=Lax");
            if (request.getSession(false) != null) {
                request.getSession(false).invalidate();
            }
        };
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
