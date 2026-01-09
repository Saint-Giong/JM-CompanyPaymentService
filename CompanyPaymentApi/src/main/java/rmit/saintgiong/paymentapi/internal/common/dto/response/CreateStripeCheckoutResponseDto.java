package rmit.saintgiong.paymentapi.internal.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStripeCheckoutResponseDto {
    /** Stripe Checkout Session ID (cs_...) */
    private String sessionId;

    /** Stripe hosted Checkout URL to redirect the browser to */
    private String checkoutUrl;

    /** Stripe Payment Intent ID (pi_...), may be null initially */
    private String paymentIntentId;
}
