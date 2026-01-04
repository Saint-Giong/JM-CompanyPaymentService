package rmit.saintgiong.paymentservice.common.exception;

import org.springframework.validation.ObjectError;
import rmit.saintgiong.profileapi.internal.common.type.DomainCode;
import rmit.saintgiong.profileapi.internal.common.type.ErrorLocation;

import java.util.Collections;
import java.util.List;

public abstract class BaseExceptionHandler {

    protected ApiError getErrorResponse(DomainCode domainCode, String message, List<ApiErrorDetails> details) {
        String errorId = String.format("API-%d", domainCode.getCode());
        return ApiError.builder()
                .errorId(errorId)
                .message(message)
                .details(details)
                .build();
    }

    protected ApiErrorDetails toErrorDetail(ObjectError error, ErrorLocation location) {
        return ApiErrorDetails.builder()
                .location(location)
                .issue(error == null ? "" : error.getDefaultMessage())
                .build();
    }

    protected ApiError getErrorResponse(DomainCode code, String message) {
        return getErrorResponse(code, message, Collections.emptyList());
    }

}
