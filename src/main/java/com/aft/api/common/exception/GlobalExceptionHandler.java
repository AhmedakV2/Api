package com.aft.api.common.exception;

import com.aft.api.common.dto.ApiError;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String TYPE_BASE = "https://docs.aft.local/errors/";

    private final Tracer tracer;

    public GlobalExceptionHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ProblemDetail> handleApi(ApiException ex, HttpServletRequest request) {
        return build(ex.code(), ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        List<ApiError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return build(ErrorCode.VALIDATION_FAILED, "Istek govdesi dogrulanamadi", request, errors);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleIntegrity(DataIntegrityViolationException ex,
                                                         HttpServletRequest request) {
        log.warn("Veri butunlugu ihlali: {}", ex.getMostSpecificCause().getMessage());
        return build(ErrorCode.CONFLICT, "Cakisan kayit", request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(ErrorCode.FORBIDDEN, ex.getMessage(), request, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuth(AuthenticationException ex, HttpServletRequest request) {
        return build(ErrorCode.UNAUTHENTICATED, ex.getMessage(), request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Beklenmeyen hata", ex);
        ProblemDetail detail = ProblemDetail.forStatus(500);
        detail.setType(URI.create(TYPE_BASE + "INTERNAL_ERROR"));
        detail.setTitle("Beklenmeyen sunucu hatasi");
        detail.setInstance(URI.create(request.getRequestURI()));
        detail.setProperty("traceId", currentTraceId());
        return ResponseEntity.status(500).body(detail);
    }

    private ResponseEntity<ProblemDetail> build(ErrorCode code, String detailText,
                                                HttpServletRequest request, List<ApiError> errors) {
        ProblemDetail detail = ProblemDetail.forStatus(code.status());
        detail.setType(URI.create(TYPE_BASE + code.name()));
        detail.setTitle(code.title());
        detail.setDetail(detailText);
        detail.setInstance(URI.create(request.getRequestURI()));
        detail.setProperty("code", code.name());
        detail.setProperty("traceId", currentTraceId());
        if (errors != null && !errors.isEmpty()) {
            detail.setProperty("errors", errors);
        }
        return ResponseEntity.status(code.status()).body(detail);
    }

    private String currentTraceId() {
        return tracer.currentSpan() == null ? null : tracer.currentSpan().context().traceId();
    }
}
