package com.headspace.shared.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HeadSpaceProblemDetail(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        String code,
        Instant timestamp,
        String correlationId,
        List<FieldViolation> violations
) {
    public HeadSpaceProblemDetail {
        Objects.requireNonNull(type);
        Objects.requireNonNull(title);
        Objects.requireNonNull(detail);
        Objects.requireNonNull(instance);
        Objects.requireNonNull(code);
        Objects.requireNonNull(timestamp);
        Objects.requireNonNull(correlationId);
    }
}
