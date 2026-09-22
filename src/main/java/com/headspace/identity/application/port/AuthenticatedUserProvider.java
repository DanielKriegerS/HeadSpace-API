package com.headspace.identity.application.port;

import com.headspace.identity.domain.model.User;

import java.util.Optional;

public interface AuthenticatedUserProvider {
    Optional<User> getCurrentUser();
}
