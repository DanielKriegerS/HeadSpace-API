package com.headspace.shared.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.headspace.shared.api.HeadSpaceProblemDetail;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RestAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final HeadSpaceProblemFactory problemFactory;

    public RestAuthenticationEntryPoint(
            ObjectMapper objectMapper,
            HeadSpaceProblemFactory problemFactory) {

        this.objectMapper = objectMapper;
        this.problemFactory = problemFactory;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {

        HeadSpaceProblemDetail problem =
                problemFactory.authenticationRequired(request);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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