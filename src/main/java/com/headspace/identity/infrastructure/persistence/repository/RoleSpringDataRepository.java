package com.headspace.identity.infrastructure.persistence.repository;

import com.headspace.identity.domain.model.RoleName;
import com.headspace.identity.infrastructure.persistence.entity.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleSpringDataRepository extends JpaRepository<RoleJpaEntity, UUID> {
    Optional<RoleJpaEntity> findByName(RoleName name);
}
