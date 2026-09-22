package com.headspace.identity.application.usecase;

import com.headspace.identity.application.port.AuthenticatedUserProvider;
import com.headspace.identity.application.result.GetCurrentUserResult;
import com.headspace.identity.domain.model.RoleName;
import com.headspace.identity.domain.model.User;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class GetCurrentUserUseCase {
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public GetCurrentUserUseCase(AuthenticatedUserProvider authenticatedUserProvider) {
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    public GetCurrentUserResult execute() {
        User user = authenticatedUserProvider.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Authentication required"));

        List<RoleName> roles = user.getRoles().stream()
                .sorted(Comparator.comparing(Enum::name))
                .toList();

        return new GetCurrentUserResult(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileImageUrl(),
                user.getStatus(),
                roles,
                user.getCreatedAt()
        );
    }
}
