package rmit.saintgiong.paymentapi.external.services.stripe;

import rmit.saintgiong.paymentapi.internal.common.dto.request.CreateStripeCheckoutRequestDto;
import rmit.saintgiong.paymentapi.internal.common.dto.response.CreateStripeCheckoutResponseDto;
import com.stripe.model.Event;

public interface ExternalStripeCheckoutInterface {
    CreateStripeCheckoutResponseDto createStripeCheckout(CreateStripeCheckoutRequestDto req);

    void handleCheckoutSessionCompleted(Event event, String payload);

    void handleCheckoutSessionAsyncSucceeded(Event event, String payload);

    void handleCheckoutSessionAsyncFailed(Event event, String payload);

    void handlePaymentIntentSucceeded(Event event);

    void handlePaymentIntentFailed(Event event);
}
