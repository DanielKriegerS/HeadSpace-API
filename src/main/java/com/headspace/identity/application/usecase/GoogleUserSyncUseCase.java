package com.headspace.identity.application.usecase;

import com.headspace.identity.application.port.UserIdentityPort;
import com.headspace.identity.domain.exception.UserBannedException;
import com.headspace.identity.domain.model.AuthProvider;
import com.headspace.identity.domain.model.RoleName;
import com.headspace.identity.domain.model.User;
import com.headspace.identity.domain.model.UserId;
import com.headspace.identity.domain.model.UserStatus;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class GoogleUserSyncUseCase {
    private final UserIdentityPort userIdentityPort;

    public GoogleUserSyncUseCase(UserIdentityPort userIdentityPort) {
        this.userIdentityPort = userIdentityPort;
    }

    public User sync(OidcUser oidcUser) {
        String providerSubject = oidcUser.getSubject();
        String email = Optional.ofNullable(oidcUser.getEmail())
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .orElseThrow(() -> new IllegalArgumentException("Email is required"));

        String preferredName = Optional.ofNullable(oidcUser.getFullName())
                .filter(value -> !value.isBlank())
                .orElseGet(() -> extractLocalPart(email));
        String profileImageUrl = Optional.ofNullable(oidcUser.getAttribute("picture"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .orElse(null);

        return userIdentityPort.findByProviderAndSubject(AuthProvider.GOOGLE, providerSubject)
                .map(existing -> handleExistingUser(existing, email, preferredName, profileImageUrl))
                .orElseGet(() -> createNewUser(providerSubject, email, preferredName, profileImageUrl));
    }

    private User handleExistingUser(User existing, String email, String preferredName, String profileImageUrl) {
        if (existing.getStatus() == UserStatus.BANNED) {
            throw new UserBannedException("User is banned and cannot authenticate");
        }

        String usernameCandidate = generateUniqueUsername(userIdentityPort, preferredName, email, existing.getUsername());
        User updated = existing.withProfile(email, usernameCandidate, profileImageUrl);
        return userIdentityPort.save(updated);
    }

    private User createNewUser(String providerSubject, String email, String preferredName, String profileImageUrl) {
        String username = generateUniqueUsername(userIdentityPort, preferredName, email, null);
        User user = User.createNew(
                UserId.generate().value(),
                AuthProvider.GOOGLE,
                providerSubject,
                email,
                username,
                profileImageUrl,
                UserStatus.ACTIVE,
                Set.of(RoleName.ROLE_USER),
                Instant.now(),
                Instant.now()
        );
        return userIdentityPort.save(user);
    }

    public static String generateUniqueUsername(UserIdentityPort port, String preferredName, String email, String excludeCurrentUsername) {
        String normalizedBase = normalizeUsername(preferredName, email);
        String candidate = normalizedBase;
        String current = excludeCurrentUsername == null ? null : excludeCurrentUsername.trim();
        int suffix = 1;

        while (isTaken(port, candidate, current)) {
            String suffixValue = Integer.toString(suffix);
            String truncated = normalizedBase;
            int maxLength = Math.max(3, 50 - suffixValue.length());
            if (truncated.length() > maxLength) {
                truncated = truncated.substring(0, maxLength);
            }
            candidate = truncated + suffixValue;
            suffix++;
            if (suffix > 1000) {
                throw new IllegalStateException("Username generation exceeded the allowed collision attempts");
            }
        }
        return candidate;
    }

    private static boolean isTaken(UserIdentityPort port, String candidate, String excludeCurrentUsername) {
        if (candidate == null || candidate.isBlank()) {
            return true;
        }
        if (excludeCurrentUsername != null && candidate.equalsIgnoreCase(excludeCurrentUsername)) {
            return false;
        }
        return port != null && port.existsByUsername(candidate);
    }

    static String normalizeUsername(String preferredName, String email) {
        String candidate = preferredName == null || preferredName.isBlank()
                ? extractLocalPart(email)
                : preferredName;

        String normalized = candidate.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-$", "")
                .replaceAll("--+", "-");

        if (normalized == null || normalized.isBlank()) {
            normalized = extractLocalPart(email);
        }
        if (normalized.length() > 50) {
            normalized = normalized.substring(0, 50);
        }
        if (normalized.length() < 3) {
            normalized = (normalized + "user").substring(0, Math.min(50, normalized.length() + 4));
        }
        return normalized;
    }

    private static String extractLocalPart(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return "headspaceuser";
        }
        return email.substring(0, email.indexOf('@')).replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-$", "")
                .toLowerCase(Locale.ROOT);
    }
}
