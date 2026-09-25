package com.headspace.identity.domain.model;

import java.time.Instant;
import java.util.*;

public final class User {
    private final UUID id;
    private final AuthProvider provider;
    private final String providerSubject;
    private final String email;
    private final String username;
    private final String profileImageUrl;
    private final UserStatus status;
    private final Set<RoleName> roles;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Long version;

    private User(
            UUID id,
            AuthProvider provider,
            String providerSubject,
            String email,
            String username,
            String profileImageUrl,
            UserStatus status,
            Set<RoleName> roles,
            Instant createdAt,
            Instant updatedAt,
            Long version
    ) {
        this.id = Objects.requireNonNull(id, "User id is required");
        this.provider = Objects.requireNonNull(provider, "Provider is required");
        this.providerSubject = requireText(providerSubject, "Provider subject is required");
        this.email = requireEmail(email);
        this.username = requireUsername(username);
        this.profileImageUrl = profileImageUrl;
        this.status = Objects.requireNonNull(status, "Status is required");
        this.roles = normalizeRoles(roles);
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt is required");
        this.version = version;
    }

    public static User createNew(
            UUID id,
            AuthProvider provider,
            String providerSubject,
            String email,
            String username,
            String profileImageUrl,
            UserStatus status,
            Set<RoleName> roles,
            Instant createdAt,
            Instant updatedAt
    ) {
        Set<RoleName> normalized = normalizeRoles(roles);
        if (!normalized.contains(RoleName.ROLE_USER)) {
            normalized = new LinkedHashSet<>(normalized);
            normalized.add(RoleName.ROLE_USER);
        }
        return new User(id, provider, providerSubject, email, username, profileImageUrl, status, normalized, createdAt, updatedAt, null);
    }

    public static User fromPersisted(
            UUID id,
            AuthProvider provider,
            String providerSubject,
            String email,
            String username,
            String profileImageUrl,
            UserStatus status,
            Set<RoleName> roles,
            Instant createdAt,
            Instant updatedAt,
            Long version
    ) {
        return new User(id, provider, providerSubject, email, username, profileImageUrl, status, roles, createdAt, updatedAt, version);
    }

    public User withProfile(String email, String username, String profileImageUrl) {
        return new User(
                this.id,
                this.provider,
                this.providerSubject,
                email,
                username,
                profileImageUrl,
                this.status,
                this.roles,
                this.createdAt,
                Instant.now(),
                this.version
        );
    }

    public boolean isAuthenticable() {
        return this.status == UserStatus.ACTIVE;
    }

    public boolean hasRole(RoleName roleName) {
        return this.roles.contains(roleName);
    }

    public User withRoles(Set<RoleName> newRoles) {
        Set<RoleName> normalized = normalizeRoles(newRoles);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("User must have at least ROLE_USER");
        }
        return new User(
                this.id,
                this.provider,
                this.providerSubject,
                this.email,
                this.username,
                this.profileImageUrl,
                this.status,
                normalized,
                this.createdAt,
                Instant.now(),
                this.version + 1
        );
    }

    public String getProviderSubject() {
        return providerSubject;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Set<RoleName> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public UUID getId() {
        return id;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private static String requireEmail(String value) {
        String email = requireText(value, "Email is required");
        if (!email.contains("@") || email.length() < 5) {
            throw new IllegalArgumentException("Email is invalid");
        }
        return email;
    }

    private static String requireUsername(String value) {
        String username = requireText(value, "Username is required");
        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException("Username length must be between 3 and 50");
        }
        return username;
    }

    private static Set<RoleName> normalizeRoles(Set<RoleName> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("User must have at least ROLE_USER");
        }
        Set<RoleName> normalized = new LinkedHashSet<>();
        for (RoleName roleName : roles) {
            if (roleName == null) {
                continue;
            }
            normalized.add(roleName);
        }
        if (!normalized.contains(RoleName.ROLE_USER)) {
            normalized.add(RoleName.ROLE_USER);
        }
        return Collections.unmodifiableSet(normalized);
    }
}
