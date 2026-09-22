package com.headspace.identity.presentation.rest;

import com.headspace.identity.application.result.GetCurrentUserResult;
import com.headspace.identity.application.usecase.GetCurrentUserUseCase;
import com.headspace.identity.presentation.rest.response.CurrentUserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CurrentUserController {
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    public CurrentUserController(GetCurrentUserUseCase getCurrentUserUseCase) {
        this.getCurrentUserUseCase = getCurrentUserUseCase;
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> getCurrentUser() {
        GetCurrentUserResult result = getCurrentUserUseCase.execute();
        CurrentUserResponse response = new CurrentUserResponse(
                result.id(),
                result.username(),
                result.email(),
                result.profileImageUrl(),
                result.status(),
                result.roles(),
                result.createdAt()
        );
        return ResponseEntity.ok(response);
    }
}
