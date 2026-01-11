package rmit.saintgiong.paymentapi.external.services.stripe;

import com.stripe.exception.SignatureVerificationException;

public interface ExternalStripeWebhookInterface {
    void handleWebhook(String payload, String sigHeader) throws SignatureVerificationException;
}
