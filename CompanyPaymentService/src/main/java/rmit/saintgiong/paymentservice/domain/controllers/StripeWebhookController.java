package rmit.saintgiong.paymentservice.domain.controllers;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import rmit.saintgiong.paymentapi.internal.common.type.TransactionStatus;
import rmit.saintgiong.paymentservice.domain.repositories.CompanyPaymentRepository;

@RestController
@RequestMapping("/stripe")
@Slf4j
public class StripeWebhookController {

    private final CompanyPaymentRepository repository;

    @Value("${stripe.webhookSecret:}")
    private String webhookSecret;

    public StripeWebhookController(CompanyPaymentRepository repository, @Value("${stripe.webhookSecret:}") String webhookSecret) {
        this.repository = repository;
        this.webhookSecret = webhookSecret;
        log.info("StripeWebhookController initialized with webhookSecret: {}", webhookSecret);
    }

    @PostMapping("/webhook")
    @Transactional
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader
    ) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            // Allow running locally without a secret; do not process.
            log.warn("method=handleWebhook, message=stripe.webhookSecret not configured; ignoring webhook");
            return ResponseEntity.ok("ignored");
        }
        if (sigHeader == null || sigHeader.isBlank()) {
            return ResponseEntity.badRequest().body("Missing Stripe-Signature header");
        }

        final Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("method=handleWebhook, message=Invalid Stripe signature: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        log.info("method=handleWebhook, message=Received Stripe event, type={}, id={}", event.getType(), event.getId());

        switch (event.getType()) {
            case "payment_intent.succeeded" -> handlePaymentIntentSucceeded(event);
            case "payment_intent.payment_failed" -> handlePaymentIntentFailed(event);
            default -> {
                // no-op
            }
        }

        return ResponseEntity.ok("ok");
    }

    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent pi = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        if (pi == null) return;

        repository.findByStripePaymentIntentId(pi.getId()).ifPresent(entity -> {
            entity.setStatus(TransactionStatus.SUCCESSFUL);

            // Store a gateway transaction id if available (charge id)
            String chargeId = null;
            if (pi.getLatestCharge() != null) {
                chargeId = String.valueOf(pi.getLatestCharge());
            }

            if (chargeId != null && !chargeId.isBlank()) {
                entity.setPaymentTransactionId(chargeId);
            }
        });
    }

    private void handlePaymentIntentFailed(Event event) {
        PaymentIntent pi = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        if (pi == null) return;

        repository.findByStripePaymentIntentId(pi.getId())
                .ifPresent(entity -> entity.setStatus(TransactionStatus.FAILED));
    }
}
