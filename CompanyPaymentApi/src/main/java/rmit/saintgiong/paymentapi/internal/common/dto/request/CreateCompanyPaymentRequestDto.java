package rmit.saintgiong.paymentapi.internal.common.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import rmit.saintgiong.paymentapi.internal.common.type.PaymentMethod;

@Data
@Builder
public class CreateCompanyPaymentRequestDto {
    @NotNull
    private String companyId;

    @NotNull
    private Double amount;

    @NotNull
    private String currency;

    @NotNull
    private PaymentMethod method;
}
