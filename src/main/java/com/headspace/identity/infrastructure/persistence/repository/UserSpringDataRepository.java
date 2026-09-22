package com.headspace.identity.infrastructure.persistence.repository;

import com.headspace.identity.domain.model.AuthProvider;
import com.headspace.identity.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserSpringDataRepository extends JpaRepository<UserJpaEntity, UUID> {
    Optional<UserJpaEntity> findByProviderAndProviderSubject(AuthProvider provider, String providerSubject);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
    Optional<UserJpaEntity> findWithRolesById(UUID id);

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
    Optional<UserJpaEntity> findWithRolesByProviderAndProviderSubject(AuthProvider provider, String providerSubject);
}
