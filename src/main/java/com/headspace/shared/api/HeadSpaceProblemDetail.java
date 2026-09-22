package com.headspace.shared.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

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
}
