package com.headspace.identity.infrastructure.security.config;

import com.headspace.identity.application.usecase.GoogleUserSyncUseCase;
import com.headspace.identity.domain.model.User;
import com.headspace.identity.infrastructure.security.principal.HeadSpaceUserPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@Configuration
public class OAuth2Config {

    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService(
            GoogleUserSyncUseCase googleUserSyncUseCase
    ) {

        OidcUserService delegate = new OidcUserService();

        return userRequest -> {

            OidcUser oidcUser =
                    delegate.loadUser(userRequest);

            User user =
                    googleUserSyncUseCase.sync(oidcUser);

            return new HeadSpaceUserPrincipal(
                    user,
                    oidcUser.getAttributes(),
                    oidcUser.getIdToken()
            );
        };
    }
}