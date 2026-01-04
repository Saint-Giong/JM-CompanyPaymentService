package rmit.saintgiong.paymentapi.internal.common.dto.response;

import lombok.Builder;
import lombok.Data;
import rmit.saintgiong.paymentapi.internal.common.type.TransactionStatus;

@Data
@Builder
public class CreateCompanyPaymentResponseDto {
    private String id;
    private TransactionStatus status;

    /** Stripe Checkout Session id (cs_...). Present when using Stripe Checkout. */
    private String stripeCheckoutSessionId;

    /** Stripe hosted Checkout URL to redirect the browser to. */
    private String checkoutUrl;
}
