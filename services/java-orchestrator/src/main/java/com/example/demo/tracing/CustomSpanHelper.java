package com.example.demo.tracing;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class CustomSpanHelper {

    private final Tracer tracer;

    public <T> T inSpan(String spanName, Function<Span, T> action) {
        return execute(tracer.spanBuilder(spanName), action);
    }

    public <T> T inNewTrace(String spanName, Function<Span, T> action) {
        return execute(tracer.spanBuilder(spanName).setNoParent(), action);
    }

    public void setAttribute(Span span, String key, Object value) {
        if (value == null) {
            return;
        }

        if (value instanceof String v) {
            span.setAttribute(key, v);
            return;
        }
        if (value instanceof Long v) {
            span.setAttribute(key, v);
            return;
        }
        if (value instanceof Integer v) {
            span.setAttribute(key, v.longValue());
            return;
        }
        if (value instanceof Double v) {
            span.setAttribute(key, v);
            return;
        }
        if (value instanceof Float v) {
            span.setAttribute(key, (double) v);
            return;
        }
        if (value instanceof Boolean v) {
            span.setAttribute(key, v);
            return;
        }
        span.setAttribute(key, value.toString());
    }

    private <T> T execute(SpanBuilder spanBuilder, Function<Span, T> action) {
        Span span = spanBuilder.startSpan();
        try (Scope ignored = span.makeCurrent()) {
            T result = action.apply(span);
            span.setStatus(StatusCode.OK);
            return result;
        } catch (RuntimeException ex) {
            span.recordException(ex);
            span.setStatus(StatusCode.ERROR, ex.getMessage());
            throw ex;
        } finally {
            span.end();
        }
    }
}
