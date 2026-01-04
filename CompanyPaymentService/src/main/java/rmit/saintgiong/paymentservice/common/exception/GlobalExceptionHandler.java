package rmit.saintgiong.paymentservice.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.validation.FieldError;
import rmit.saintgiong.profileapi.internal.common.type.DomainCode;
import rmit.saintgiong.profileapi.internal.common.type.ErrorLocation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static rmit.saintgiong.profileapi.internal.common.type.DomainCode.INTERNAL_SERVER_ERROR;
import static rmit.saintgiong.profileapi.internal.common.type.DomainCode.INVALID_REQUEST_PARAMETER;


@RestControllerAdvice
public class GlobalExceptionHandler extends BaseExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("Handling MethodArgumentNotValidException: {}", ex.getMessage());
        List<ApiErrorDetails> errorDetails = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> ApiErrorDetails.builder()
                        .location(ErrorLocation.BODY)
                        .field(fe.getField())
                        .value(Objects.toString(fe.getRejectedValue(), ""))
                        .issue(fe.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        var errRes = getErrorResponse(DomainCode.ARGUMENT_NOT_VALID, DomainCode.ARGUMENT_NOT_VALID.getMessageTemplate(), errorDetails);

        return ResponseEntity.badRequest().body(errRes);
    }

    @ExceptionHandler(rmit.saintgiong.profileservice.common.exception.DomainException.class)
    public ResponseEntity<ApiError> domainException(HttpServletRequest request, rmit.saintgiong.profileservice.common.exception.DomainException ex) {
        log.error("method=domainException, endPoint={}, exception={}", request.getRequestURI(), ex.getMessage());

        int httpCode = ex.getDomainCode().getCode() / 1000;

        var errRes = getErrorResponse(ex.getDomainCode(), ex.getMessage());
        HttpStatus status = HttpStatus.resolve(httpCode);
        if (status == null) status = HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(errRes);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiError> missingHeader(MissingRequestHeaderException ex) {
        var errRes = getErrorResponse(DomainCode.MISSING_REQUEST_HEADER, String.format(DomainCode.MISSING_REQUEST_HEADER.getMessageTemplate(), ex.getHeaderName()), Collections.emptyList());
        return ResponseEntity.badRequest().body(errRes);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> httpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        var errRes = getErrorResponse(DomainCode.METHOD_NOT_ALLOWED, String.format(DomainCode.METHOD_NOT_ALLOWED.getMessageTemplate(), ex.getMethod()), Collections.emptyList());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errRes);
    }

    // Handle invalid parameter errors such as invalid UUID format or type mismatches
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> illegalArgument(IllegalArgumentException ex) {
        // This commonly happens when UUID.fromString is given an invalid value
        log.error("method=illegalArgument, exception={}", ex.getMessage(), ex);

        ApiErrorDetails detail = ApiErrorDetails.builder()
                .location(ErrorLocation.PARAMETER)
                .issue(ex.getMessage())
                .build();

        var errRes = getErrorResponse(INVALID_REQUEST_PARAMETER,
                String.format(INVALID_REQUEST_PARAMETER.getMessageTemplate(), ex.getMessage()),
                Collections.singletonList(detail));

        return ResponseEntity.badRequest().body(errRes);
    }

    @ExceptionHandler({HandlerMethodValidationException.class})
    public ResponseEntity<ApiError> handlerMethodValidationException(HandlerMethodValidationException ex) {
        // Handle both type mismatches (e.g. wrong parameter type) and method-level validation exceptions
        log.error("method=handlerMethodValidationException, exception={}", ex.getMessage(), ex);

        var errRes = getErrorResponse(INVALID_REQUEST_PARAMETER,
                INVALID_REQUEST_PARAMETER.getMessageTemplate(),
                Collections.emptyList());

        return ResponseEntity.badRequest().body(errRes);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> exception(Exception ex) {
        log.error("method=exception, exception={}", ex.getMessage(), ex);
        var errRes = getErrorResponse(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR.getMessageTemplate(), Collections.emptyList());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errRes);
    }

    private String getDefaultMessageFromException(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(ex.getMessage());
    }
}