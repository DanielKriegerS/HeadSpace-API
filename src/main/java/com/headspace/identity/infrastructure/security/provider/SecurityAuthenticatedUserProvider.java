package com.headspace.identity.infrastructure.security.provider;

import com.headspace.identity.application.port.AuthenticatedUserProvider;
import com.headspace.identity.domain.model.User;
import com.headspace.identity.domain.model.UserStatus;
import com.headspace.identity.infrastructure.security.principal.HeadSpaceUserPrincipal;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityAuthenticatedUserProvider
        implements AuthenticatedUserProvider {

    @Override
    public Optional<User> getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            return Optional.empty();
        }

        Object principal =
                authentication.getPrincipal();

        if (!(principal instanceof HeadSpaceUserPrincipal)) {

            return Optional.empty();
        }

        User user =
                ((HeadSpaceUserPrincipal) principal).getUser();

        if (user.getStatus() == UserStatus.BANNED) {

            throw new AuthenticationCredentialsNotFoundException(
                    "User is banned");
        }

        return Optional.of(user);
    }
}