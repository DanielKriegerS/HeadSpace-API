package com.headspace.identity.infrastructure.persistence.entity;

import com.headspace.identity.domain.model.RoleName;
import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "role")
public class RoleJpaEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 32)
    private RoleName name;

    @OneToMany(mappedBy = "role")
    private Set<UserRoleJpaEntity> userRoles = new LinkedHashSet<>();

    public RoleJpaEntity() {
    }

    public RoleJpaEntity(UUID id, RoleName name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public RoleName getName() {
        return name;
    }

    public void setName(RoleName name) {
        this.name = name;
    }

    public Set<UserRoleJpaEntity> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<UserRoleJpaEntity> userRoles) {
        this.userRoles = userRoles == null ? new LinkedHashSet<>() : userRoles;
    }
}
