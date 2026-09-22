package com.headspace.identity.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_role")
public class UserRoleJpaEntity {
    @EmbeddedId
    private UserRoleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id", nullable = false)
    private RoleJpaEntity role;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    @Column(name = "assigned_by")
    private UUID assignedBy;

    public UserRoleJpaEntity() {
    }

    public UserRoleJpaEntity(UserJpaEntity user, RoleJpaEntity role, Instant assignedAt, UUID assignedBy) {
        this.user = user;
        this.role = role;
        this.assignedAt = assignedAt;
        this.assignedBy = assignedBy;
        this.id = new UserRoleId(user.getId(), role.getId());
    }

    public UserRoleId getId() {
        return id;
    }

    public void setId(UserRoleId id) {
        this.id = id;
    }

    public UserJpaEntity getUser() {
        return user;
    }

    public void setUser(UserJpaEntity user) {
        this.user = user;
        if (this.id == null && user != null) {
            this.id = new UserRoleId();
        }
        if (this.id != null && user != null) {
            this.id.setUserId(user.getId());
        }
    }

    public RoleJpaEntity getRole() {
        return role;
    }

    public void setRole(RoleJpaEntity role) {
        this.role = role;
        if (this.id == null && role != null) {
            this.id = new UserRoleId();
        }
        if (this.id != null && role != null) {
            this.id.setRoleId(role.getId());
        }
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }

    public UUID getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(UUID assignedBy) {
        this.assignedBy = assignedBy;
    }
}
