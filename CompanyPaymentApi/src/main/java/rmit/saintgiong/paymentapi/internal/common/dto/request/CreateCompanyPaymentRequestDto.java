package rmit.saintgiong.paymentapi.internal.common.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rmit.saintgiong.paymentapi.internal.common.type.PaymentMethod;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
