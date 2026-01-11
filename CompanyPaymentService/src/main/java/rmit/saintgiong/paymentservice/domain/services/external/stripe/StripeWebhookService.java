package rmit.saintgiong.paymentservice.domain.services.external.stripe;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rmit.saintgiong.paymentapi.external.services.stripe.ExternalStripeCheckoutInterface;
import rmit.saintgiong.paymentapi.external.services.stripe.ExternalStripeWebhookInterface;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookService implements ExternalStripeWebhookInterface {

    private final ExternalStripeCheckoutInterface stripeCheckoutInterface;

    @Value("${stripe.webhookSecret:}")
    private String webhookSecret;

    @Override
    @Transactional
    public void handleWebhook(String payload, String sigHeader) throws SignatureVerificationException {
        validateWebhookConfiguration(sigHeader);

        Event event = constructAndVerifyEvent(payload, sigHeader);

        log.info("method=handleWebhook, message=Received Stripe event, type={}, id={}",
                event.getType(), event.getId());

        switch (event.getType()) {
            // Stripe Checkout events
            case "checkout.session.completed" ->
                    stripeCheckoutInterface.handleCheckoutSessionCompleted(event, payload);

            case "checkout.session.async_payment_succeeded" ->
                    stripeCheckoutInterface.handleCheckoutSessionAsyncSucceeded(event, payload);

            case "checkout.session.async_payment_failed" ->
                    stripeCheckoutInterface.handleCheckoutSessionAsyncFailed(event, payload);

            // Fallback/legacy PaymentIntent flow
            case "payment_intent.succeeded" ->
                    stripeCheckoutInterface.handlePaymentIntentSucceeded(event);

            case "payment_intent.payment_failed" ->
                    stripeCheckoutInterface.handlePaymentIntentFailed(event);

            default ->
                    log.debug("method=routeEventToHandler, message=Unhandled event type: {}", event.getType());
        }
    }

    private void validateWebhookConfiguration(String sigHeader) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("method=validateWebhookConfiguration, message=stripe.webhookSecret not configured");
            throw new IllegalArgumentException("Webhook secret not configured");
        }

        if (sigHeader == null || sigHeader.isBlank()) {
            throw new IllegalArgumentException("Missing Stripe-Signature header");
        }
    }

    private Event constructAndVerifyEvent(String payload, String sigHeader) throws SignatureVerificationException {
        try {
            return Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("method=constructAndVerifyEvent, message=Invalid Stripe signature: {}", e.getMessage());
            throw new SignatureVerificationException(
                    "Invalid webhook signature",
                    sigHeader
            );
        }
    }
}
