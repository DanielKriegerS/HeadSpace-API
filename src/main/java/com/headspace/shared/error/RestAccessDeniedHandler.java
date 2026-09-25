package com.headspace.shared.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.headspace.shared.api.HeadSpaceProblemDetail;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RestAccessDeniedHandler
        implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final HeadSpaceProblemFactory problemFactory;

    public RestAccessDeniedHandler(
            ObjectMapper objectMapper,
            HeadSpaceProblemFactory problemFactory) {

        this.objectMapper = objectMapper;
        this.problemFactory = problemFactory;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception)
            throws IOException, ServletException {

        HeadSpaceProblemDetail problem =
                problemFactory.accessDenied(request);

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(
                MediaType.APPLICATION_PROBLEM_JSON_VALUE
        );

        if (problem.correlationId() != null) {
            response.setHeader(
                    "X-Correlation-Id",
                    problem.correlationId()
            );
        }

        objectMapper.writeValue(
                response.getOutputStream(),
                problem
        );
    }
}