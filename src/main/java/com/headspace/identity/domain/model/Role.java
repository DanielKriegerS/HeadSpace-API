package com.headspace.identity.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class Role {
    private final UUID id;
    private final RoleName name;

    public Role(UUID id, RoleName name) {
        this.id = Objects.requireNonNull(id, "Role id is required");
        this.name = Objects.requireNonNull(name, "Role name is required");
    }

    public UUID getId() {
        return id;
    }

    public RoleName getName() {
        return name;
    }
}
