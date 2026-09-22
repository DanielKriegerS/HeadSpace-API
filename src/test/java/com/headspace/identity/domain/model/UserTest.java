package com.headspace.identity.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    void shouldCreateValidUserWithRoleUser() {
        User user = User.createNew(
                UUID.randomUUID(),
                AuthProvider.GOOGLE,
                "provider-subject-1",
                "daniel@example.com",
                "daniel",
                null,
                UserStatus.ACTIVE,
                Set.of(RoleName.ROLE_USER),
                Instant.now(),
                Instant.now()
        );

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getRoles()).contains(RoleName.ROLE_USER);
        assertThat(user.isAuthenticable()).isTrue();
    }

    @Test
    void shouldRejectMissingRequiredData() {
        assertThatThrownBy(() -> User.createNew(
                UUID.randomUUID(),
                AuthProvider.GOOGLE,
                "",
                "daniel@example.com",
                "daniel",
                null,
                UserStatus.ACTIVE,
                Set.of(RoleName.ROLE_USER),
                Instant.now(),
                Instant.now()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectBannedUserAsAuthenticable() {
        User user = User.createNew(
                UUID.randomUUID(),
                AuthProvider.GOOGLE,
                "provider-subject-2",
                "blocked@example.com",
                "blocked",
                null,
                UserStatus.BANNED,
                Set.of(RoleName.ROLE_USER),
                Instant.now(),
                Instant.now()
        );

        assertThat(user.isAuthenticable()).isFalse();
    }
}
