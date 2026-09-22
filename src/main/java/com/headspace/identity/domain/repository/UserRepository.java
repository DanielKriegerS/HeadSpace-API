package com.headspace.identity.domain.repository;

import com.headspace.identity.domain.model.AuthProvider;
import com.headspace.identity.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findByProviderAndSubject(AuthProvider provider, String providerSubject);
    Optional<User> findById(UUID id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User save(User user);
}
