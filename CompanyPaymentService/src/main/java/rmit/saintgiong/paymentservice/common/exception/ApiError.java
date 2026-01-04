package rmit.saintgiong.paymentservice.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Getter;
import rmit.saintgiong.paymentservice.common.exception.ApiErrorDetails;

import java.util.List;

@Builder
@Getter
@JsonInclude(Include.NON_EMPTY)
public class ApiError {

    private String errorId;
    private String message;
    private List<ApiErrorDetails> details;
}
