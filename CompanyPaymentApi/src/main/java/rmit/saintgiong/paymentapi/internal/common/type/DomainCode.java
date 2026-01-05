package rmit.saintgiong.paymentapi.internal.common.type;

import lombok.Getter;

@Getter
public enum DomainCode {

    // 4xx Client Errors
    MISSING_REQUEST_HEADER(400_000, "The required request header is missing: %s"),

    ARGUMENT_NOT_VALID(400_001, "The argument provided is not valid"),

    INVALID_REQUEST_PARAMETER(400_002, "The request parameter is invalid"),

    INVALID_BUSINESS_LOGIC(400_003, "The request is invalid due to business logic: %s"),

    UNAUTHORIZED_ACCESS(401_000, "Unauthorized: %s"),

    FORBIDDEN_ACCESS(403_000, "Forbidden: %s"),

    RESOURCE_NOT_FOUND(404_000, "The requested resource is not found: %s"),

    METHOD_NOT_ALLOWED(405_000, "The HTTP method is not allowed: %s"),

    TOO_MANY_REQUESTS(429_000, "Too many requests: %s"),

    // 5xx Server Errors
    INTERNAL_SERVER_ERROR(500_000, "An internal server error occurred: %s"),

    SERVICE_UNAVAILABLE(503_000, "The service is currently unavailable: %s");

    private final int code;

    private final String messageTemplate;

    DomainCode(int code, String messageTemplate) {
        this.code = code;
        this.messageTemplate = messageTemplate;
    }

    public String getMessageTemplate() {
        return name() + "(" + code + ")" + " - " + messageTemplate;
    }

    public String getMessageString() {
        return messageTemplate;
    }
}
