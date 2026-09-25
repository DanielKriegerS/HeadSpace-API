package com.headspace.shared.error;

import com.headspace.shared.api.HeadSpaceProblemDetail;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class HeadSpaceProblemFactory {

    public HeadSpaceProblemDetail authenticationRequired(
            HttpServletRequest request) {

        return create(
                request,
                HttpStatus.UNAUTHORIZED,
                "authentication-required",
                "Autenticação necessária",
                "É necessário autenticar-se para acessar este recurso.",
                "AUTHENTICATION_REQUIRED"
        );
    }

    public HeadSpaceProblemDetail accessDenied(
            HttpServletRequest request) {

        return create(
                request,
                HttpStatus.FORBIDDEN,
                "access-denied",
                "Acesso negado",
                "O usuário não possui permissão para acessar este recurso.",
                "ACCESS_DENIED"
        );
    }

    public HeadSpaceProblemDetail userBanned(
            HttpServletRequest request) {

        return create(
                request,
                HttpStatus.FORBIDDEN,
                "user-banned",
                "Usuário bloqueado",
                "O usuário está bloqueado e não pode acessar este recurso.",
                "USER_BANNED"
        );
    }

    private HeadSpaceProblemDetail create(
            HttpServletRequest request,
            HttpStatus status,
            String problemType,
            String title,
            String detail,
            String code) {

        return new HeadSpaceProblemDetail(
                "https://headspace.app/problems/" + problemType,
                title,
                status.value(),
                detail,
                request.getRequestURI(),
                code,
                Instant.now(),
                correlationId(),
                null
        );
    }

    private String correlationId() {
        String correlationId = MDC.get("correlationId");

        return correlationId == null || correlationId.isBlank()
                ? "UNAVAILABLE"
                : correlationId;
    }
}