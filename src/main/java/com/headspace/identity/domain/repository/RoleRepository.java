package com.headspace.identity.domain.repository;

import com.headspace.identity.domain.model.Role;
import com.headspace.identity.domain.model.RoleName;

import java.util.Optional;

public interface RoleRepository {
    Optional<Role> findByName(RoleName name);
}
