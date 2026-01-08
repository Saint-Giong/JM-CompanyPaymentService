package rmit.saintgiong.paymentapi.internal.common.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import rmit.saintgiong.paymentapi.internal.common.type.PaymentMethod;

import java.util.Map;

@Data
@Builder
public class CreateStripeCheckoutRequestDto {
    @NotNull
    private Double amount;

    @NotNull
    private String currency;

    @NotNull
    private String successUrl;

    @NotNull
    private String cancelUrl;

    private String description;

    private PaymentMethod method;

    private Map<String, String> metadata;
}
