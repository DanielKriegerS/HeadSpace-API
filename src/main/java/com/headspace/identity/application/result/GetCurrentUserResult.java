package com.headspace.identity.application.result;

import com.headspace.identity.domain.model.RoleName;
import com.headspace.identity.domain.model.UserStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GetCurrentUserResult(
        UUID id,
        String username,
        String email,
        String profileImageUrl,
        UserStatus status,
        List<RoleName> roles,
        Instant createdAt
) {
}
