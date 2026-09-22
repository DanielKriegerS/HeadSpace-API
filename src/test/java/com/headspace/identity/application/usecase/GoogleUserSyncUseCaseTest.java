package com.headspace.identity.application.usecase;

import com.headspace.identity.application.port.UserIdentityPort;
import com.headspace.identity.domain.model.AuthProvider;
import com.headspace.identity.domain.model.RoleName;
import com.headspace.identity.domain.model.User;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GoogleUserSyncUseCaseTest {

    @Test
    void shouldNormalizeUsernameAndAddSuffixOnCollision() {
        var port = new StubUserIdentityPort();
        var service = new GoogleUserSyncUseCase(port);

        String normalized = GoogleUserSyncUseCase.normalizeUsername("Daniel!", "daniel@example.com");
        assertThat(normalized).isEqualTo("daniel");

        String unique = GoogleUserSyncUseCase.generateUniqueUsername(port, "Daniel!", "daniel@example.com", null);
        assertThat(unique).isEqualTo("daniel1");
    }

    private static final class StubUserIdentityPort implements UserIdentityPort {
        @Override
        public Optional<User> findByProviderAndSubject(AuthProvider provider, String providerSubject) {
            return Optional.empty();
        }

        @Override
        public Optional<User> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public boolean existsByUsername(String username) {
            return "daniel".equalsIgnoreCase(username);
        }

        @Override
        public boolean existsByEmail(String email) {
            return false;
        }

        @Override
        public User save(User user) {
            return user;
        }

        @Override
        public Optional<com.headspace.identity.domain.model.Role> findRoleByName(RoleName roleName) {
            return Optional.of(new com.headspace.identity.domain.model.Role(UUID.randomUUID(), roleName));
        }
    }
}
