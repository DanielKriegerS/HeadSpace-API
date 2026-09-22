package com.headspace.identity.presentation.rest.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.headspace.identity.domain.model.RoleName;
import com.headspace.identity.domain.model.UserStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CurrentUserResponse(
        UUID id,
        String username,
        String email,
        String profileImageUrl,
        UserStatus status,
        List<RoleName> roles,
        Instant createdAt
) {
}
