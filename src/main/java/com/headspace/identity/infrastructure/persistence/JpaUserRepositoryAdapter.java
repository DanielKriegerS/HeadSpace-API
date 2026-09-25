package com.headspace.identity.infrastructure.persistence;

import com.headspace.identity.application.port.UserIdentityPort;
import com.headspace.identity.domain.model.AuthProvider;
import com.headspace.identity.domain.model.Role;
import com.headspace.identity.domain.model.RoleName;
import com.headspace.identity.domain.model.User;
import com.headspace.identity.domain.repository.RoleRepository;
import com.headspace.identity.domain.repository.UserRepository;
import com.headspace.identity.infrastructure.persistence.entity.RoleJpaEntity;
import com.headspace.identity.infrastructure.persistence.entity.UserJpaEntity;
import com.headspace.identity.infrastructure.persistence.entity.UserRoleJpaEntity;
import com.headspace.identity.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.headspace.identity.infrastructure.persistence.repository.RoleSpringDataRepository;
import com.headspace.identity.infrastructure.persistence.repository.UserSpringDataRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class JpaUserRepositoryAdapter implements UserIdentityPort, UserRepository, RoleRepository {
    private final UserSpringDataRepository userRepository;
    private final RoleSpringDataRepository roleRepository;
    private final UserPersistenceMapper mapper;

    public JpaUserRepositoryAdapter(
            UserSpringDataRepository userRepository,
            RoleSpringDataRepository roleRepository,
            UserPersistenceMapper mapper
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<User> findByProviderAndSubject(AuthProvider provider, String providerSubject) {
        return userRepository.findWithRolesByProviderAndProviderSubject(provider, providerSubject)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findWithRolesById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public User save(User user) {

        UserJpaEntity entity = loadOrCreate(user);

        updateBasicFields(entity, user);

        synchronizeRoles(entity, user.getRoles());

        UserJpaEntity saved =
                userRepository.saveAndFlush(entity);

        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Role> findRoleByName(RoleName roleName) {
        return roleRepository.findByName(roleName).map(entity -> new Role(entity.getId(), entity.getName()));
    }

    @Override
    public Optional<Role> findByName(RoleName name) {
        return findRoleByName(name);
    }

    private UserJpaEntity loadOrCreate(User user) {
        return userRepository.findById(user.getId())
                .orElseGet(() -> mapper.toJpa(user));
    }

    private void updateBasicFields(
            UserJpaEntity entity,
            User user) {

        if (entity.getId() == null) {
            entity.setId(user.getId());
        }

        entity.setProvider(user.getProvider());
        entity.setProviderSubject(user.getProviderSubject());
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setProfileImageUrl(user.getProfileImageUrl());
        entity.setStatus(user.getStatus());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
    }

    private void synchronizeRoles(
            UserJpaEntity entity,
            Set<RoleName> desiredRoles) {

        Set<RoleName> currentRoles = entity.getUserRoles()
                .stream()
                .map(userRole -> userRole.getRole().getName())
                .collect(Collectors.toSet());

        if (currentRoles.equals(desiredRoles)) {
            return;
        }

        removeUnusedRoles(entity, desiredRoles);

        addMissingRoles(entity, desiredRoles);
    }

    private void removeUnusedRoles(
            UserJpaEntity entity,
            Set<RoleName> desiredRoles) {

        entity.getUserRoles().removeIf(userRole ->
                !desiredRoles.contains(
                        userRole.getRole().getName()
                )
        );
    }

    private void addMissingRoles(
            UserJpaEntity entity,
            Set<RoleName> desiredRoles) {

        for (RoleName roleName : desiredRoles) {

            boolean alreadyExists = entity.getUserRoles()
                    .stream()
                    .anyMatch(userRole ->
                            userRole.getRole().getName() == roleName);

            if (alreadyExists) {
                continue;
            }

            RoleJpaEntity roleEntity = roleRepository.findByName(roleName)
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Role not found: " + roleName));

            entity.getUserRoles().add(
                    new UserRoleJpaEntity(
                            entity,
                            roleEntity,
                            Instant.now(),
                            null
                    )
            );
        }
    }
}
