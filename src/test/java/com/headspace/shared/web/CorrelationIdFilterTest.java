package com.headspace.shared.web;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CorrelationIdFilterTest {

    @Test
    void shouldPreserveValidCorrelationId() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Correlation-Id")).thenReturn("abc-123");

        assertThat(CorrelationIdFilter.isValid("abc-123")).isTrue();
        assertThat(CorrelationIdFilter.resolveCorrelationId(request)).isEqualTo("abc-123");
    }

    @Test
    void shouldReplaceInvalidCorrelationId() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Correlation-Id")).thenReturn("bad\nvalue");

        assertThat(CorrelationIdFilter.isValid("bad\nvalue")).isFalse();
        assertThat(CorrelationIdFilter.resolveCorrelationId(request)).isNotBlank();
    }
}
