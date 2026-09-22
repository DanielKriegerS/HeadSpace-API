package com.headspace.identity.application.port;

import com.headspace.identity.domain.model.AuthProvider;
import com.headspace.identity.domain.model.Role;
import com.headspace.identity.domain.model.RoleName;
import com.headspace.identity.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserIdentityPort {
    Optional<User> findByProviderAndSubject(AuthProvider provider, String providerSubject);
    Optional<User> findById(UUID id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User save(User user);
    Optional<Role> findRoleByName(RoleName roleName);
}
