package rmit.saintgiong.paymentservice.common.exception.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Getter;
import rmit.saintgiong.paymentapi.internal.common.type.ErrorLocation;

@Builder
@Getter
@JsonInclude(Include.NON_EMPTY)
public class ApiErrorDetails {
    private ErrorLocation location;
    private String field;
    private Object value;
    private String issue;
}
