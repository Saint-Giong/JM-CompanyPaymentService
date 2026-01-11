package rmit.saintgiong.paymentapi.internal.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rmit.saintgiong.paymentapi.internal.common.type.TransactionStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompanyPaymentResponseDto {
    private String id;
    private TransactionStatus status;

    /** Stripe Checkout Session id (cs_...). Present when using Stripe Checkout. */
    private String stripeCheckoutSessionId;

    /** Stripe hosted Checkout URL to redirect the browser to. */
    private String checkoutUrl;
}
