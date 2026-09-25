package com.headspace.identity.infrastructure.persistence.mapper;

import com.headspace.identity.domain.model.RoleName;
import com.headspace.identity.domain.model.User;
import com.headspace.identity.infrastructure.persistence.entity.UserJpaEntity;
import org.mapstruct.Mapper;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public class UserPersistenceMapper {

    public UserJpaEntity toJpa(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId());
        entity.setProvider(user.getProvider());
        entity.setProviderSubject(user.getProviderSubject());
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setProfileImageUrl(user.getProfileImageUrl());
        entity.setStatus(user.getStatus());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        entity.setVersion(user.getVersion());

        return entity;
    }

    public User toDomain(UserJpaEntity entity) {
        Set<RoleName> roles = entity.getUserRoles() == null ? Set.of() : entity.getUserRoles().stream()
                .filter(userRole -> userRole.getRole() != null)
                .map(userRole -> userRole.getRole().getName())
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

        return User.fromPersisted(
                entity.getId(),
                entity.getProvider(),
                entity.getProviderSubject(),
                entity.getEmail(),
                entity.getUsername(),
                entity.getProfileImageUrl(),
                entity.getStatus(),
                roles,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion() == null ? null : entity.getVersion()
        );
    }
}
