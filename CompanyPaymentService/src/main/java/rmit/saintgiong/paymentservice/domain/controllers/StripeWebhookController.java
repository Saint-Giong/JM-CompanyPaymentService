package rmit.saintgiong.paymentservice.domain.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import rmit.saintgiong.paymentservice.domain.repositories.CompanyPaymentRepository;
import rmit.saintgiong.paymentservice.domain.repositories.entities.CompanyPaymentEntity;
import rmit.saintgiong.paymentservice.domain.services.StripeCheckoutService;
import rmit.saintgiong.paymentapi.internal.common.dto.request.CreateStripeCheckoutRequestDto;
import rmit.saintgiong.paymentapi.internal.common.dto.response.CreateStripeCheckoutResponseDto;

import java.util.Map;
import java.util.UUID;

import static rmit.saintgiong.paymentapi.internal.common.type.TransactionStatus.FAILED;
import static rmit.saintgiong.paymentapi.internal.common.type.TransactionStatus.SUCCESSFUL;

@RestController
@RequestMapping("/stripe")
@Slf4j
public class StripeWebhookController {

    private final CompanyPaymentRepository repository;
    private final ObjectMapper objectMapper;
    private final StripeCheckoutService stripeCheckoutService;

    @Value("${stripe.webhookSecret:}")
    private final String webhookSecret;

    public StripeWebhookController(
            CompanyPaymentRepository repository,
            ObjectMapper objectMapper,
            StripeCheckoutService stripeCheckoutService,
            @Value("${stripe.webhookSecret:}") String webhookSecret
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.stripeCheckoutService = stripeCheckoutService;
        this.webhookSecret = webhookSecret;
        log.info("StripeWebhookController initialized");
    }

    @PostMapping("/webhook")
    @Transactional
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader
    ) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
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
            // Stripe Checkout
            case "checkout.session.completed" -> handleCheckoutSessionCompleted(event, payload);
            case "checkout.session.async_payment_succeeded" -> handleCheckoutSessionAsyncSucceeded(event, payload);
            case "checkout.session.async_payment_failed" -> handleCheckoutSessionAsyncFailed(event, payload);

            // Fallback/legacy PaymentIntent flow
            case "payment_intent.succeeded" -> handlePaymentIntentSucceeded(event);
            case "payment_intent.payment_failed" -> handlePaymentIntentFailed(event);
            default -> {
                // no-op
            }
        }

        return ResponseEntity.ok("ok");
    }

    /**
     * Create Stripe checkout session for external teams (e.g., JA)
     * Does NOT save to database - only creates Stripe session
     */
    @PostMapping("/checkout-session")
    public ResponseEntity<CreateStripeCheckoutResponseDto> createCheckoutSession(
            @RequestBody CreateStripeCheckoutRequestDto request) {
        
        CreateStripeCheckoutResponseDto response = stripeCheckoutService.createStripeCheckout(request);
        return ResponseEntity.ok(response);
    }

    private void handleCheckoutSessionCompleted(Event event, String payload) {
        CheckoutSessionInfo info = extractCheckoutSessionInfo(event, payload);
        if (info == null) return;

        if (!"paid".equalsIgnoreCase(info.paymentStatus)) {
            log.info("method=handleCheckoutSessionCompleted, message=session not paid yet, sessionId={}, paymentStatus={}", info.sessionId, info.paymentStatus);
            return;
        }

        CompanyPaymentEntity entity = findByCheckoutSessionInfo(info);
        if (entity == null) return;

        entity.setStatus(SUCCESSFUL);
        if (info.paymentIntentId != null && !info.paymentIntentId.isBlank()) {
            entity.setPaymentTransactionId(info.paymentIntentId);
        }
        repository.save(entity);
    }

    private void handleCheckoutSessionAsyncSucceeded(Event event, String payload) {
        CheckoutSessionInfo info = extractCheckoutSessionInfo(event, payload);
        if (info == null) return;

        CompanyPaymentEntity entity = findByCheckoutSessionInfo(info);
        if (entity == null) return;

        entity.setStatus(SUCCESSFUL);
        if (info.paymentIntentId != null && !info.paymentIntentId.isBlank()) {
            entity.setPaymentTransactionId(info.paymentIntentId);
        }
        repository.save(entity);
    }

    private void handleCheckoutSessionAsyncFailed(Event event, String payload) {
        CheckoutSessionInfo info = extractCheckoutSessionInfo(event, payload);
        if (info == null) return;

        CompanyPaymentEntity entity = findByCheckoutSessionInfo(info);
        if (entity == null) return;

        entity.setStatus(FAILED);
        repository.save(entity);
    }

    private CompanyPaymentEntity findByCheckoutSessionInfo(CheckoutSessionInfo info) {
        if (info.sessionId != null && !info.sessionId.isBlank()) {
            var bySession = repository.findByStripeCheckoutSessionId(info.sessionId);
            if (bySession.isPresent()) return bySession.get();
        }

        if (info.paymentIntentId != null && !info.paymentIntentId.isBlank()) {
            var byIntent = repository.findByStripePaymentIntentId(info.paymentIntentId);
            if (byIntent.isPresent()) return byIntent.get();
        }

        if (info.clientReferenceId != null && !info.clientReferenceId.isBlank()) {
            try {
                UUID id = UUID.fromString(info.clientReferenceId);
                return repository.findById(id).orElse(null);
            } catch (IllegalArgumentException ignored) {
                // ignore
            }
        }

        if (info.paymentIdFromMetadata != null && !info.paymentIdFromMetadata.isBlank()) {
            try {
                UUID id = UUID.fromString(info.paymentIdFromMetadata);
                return repository.findById(id).orElse(null);
            } catch (IllegalArgumentException ignored) {
                // ignore
            }
        }

        log.warn("method=findByCheckoutSessionInfo, message=No matching payment found for checkout session, sessionId={}, paymentIntentId={}", info.sessionId, info.paymentIntentId);
        return null;
    }

    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent pi = deserializeOrNull(event, PaymentIntent.class);
        if (pi == null) return;

        repository.findByStripePaymentIntentId(pi.getId()).ifPresent(entity -> {
            entity.setStatus(SUCCESSFUL);

            String chargeId = null;
            if (pi.getLatestCharge() != null) {
                chargeId = String.valueOf(pi.getLatestCharge());
            }
            if (chargeId != null && !chargeId.isBlank()) {
                entity.setPaymentTransactionId(chargeId);
            }

            repository.save(entity);
        });
    }

    private void handlePaymentIntentFailed(Event event) {
        PaymentIntent pi = deserializeOrNull(event, PaymentIntent.class);
        if (pi == null) return;

        repository.findByStripePaymentIntentId(pi.getId())
                .ifPresent(entity -> {
                    entity.setStatus(FAILED);
                    repository.save(entity);
                });
    }

    private CheckoutSessionInfo extractCheckoutSessionInfo(Event event, String payload) {
        // First try Stripe's object deserializer.
        Session session = deserializeOrNull(event, Session.class);
        if (session != null) {
            String paymentIdFromMetadata = null;
            Map<String, String> metadata = session.getMetadata();
            if (metadata != null) {
                paymentIdFromMetadata = metadata.get("paymentId");
            }

            return new CheckoutSessionInfo(
                    session.getId(),
                    session.getPaymentStatus(),
                    session.getPaymentIntent(),
                    session.getClientReferenceId(),
                    paymentIdFromMetadata
            );
        }

        // Fallback: parse raw JSON payload (works even when Stripe SDK cannot deserialize).
        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode obj = root.path("data").path("object");
            if (obj.isMissingNode() || obj.isNull()) {
                log.warn("method=extractCheckoutSessionInfo, message=Missing data.object in Stripe payload for type={}", event.getType());
                return null;
            }

            String sessionId = textOrNull(obj.path("id"));
            String paymentStatus = textOrNull(obj.path("payment_status"));
            String paymentIntentId = textOrNull(obj.path("payment_intent"));
            String clientReferenceId = textOrNull(obj.path("client_reference_id"));

            String paymentIdFromMetadata = null;
            JsonNode metadataNode = obj.path("metadata");
            if (metadataNode != null && metadataNode.isObject()) {
                paymentIdFromMetadata = textOrNull(metadataNode.path("paymentId"));
            }

            return new CheckoutSessionInfo(sessionId, paymentStatus, paymentIntentId, clientReferenceId, paymentIdFromMetadata);
        } catch (Exception e) {
            log.warn("method=extractCheckoutSessionInfo, message=Failed to parse Stripe payload JSON, type={}, err={}", event.getType(), e.getMessage());
            return null;
        }
    }

    private String textOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        String val = node.asText(null);
        return (val == null || val.isBlank()) ? null : val;
    }

    private <T> T deserializeOrNull(Event event, Class<T> clazz) {
        Object obj = event.getDataObjectDeserializer().getObject().orElse(null);
        if (clazz.isInstance(obj)) {
            return clazz.cast(obj);
        }
        return null;
    }

    private static class CheckoutSessionInfo {
        final String sessionId;
        final String paymentStatus;
        final String paymentIntentId;
        final String clientReferenceId;
        final String paymentIdFromMetadata;

        CheckoutSessionInfo(String sessionId, String paymentStatus, String paymentIntentId, String clientReferenceId, String paymentIdFromMetadata) {
            this.sessionId = sessionId;
            this.paymentStatus = paymentStatus;
            this.paymentIntentId = paymentIntentId;
            this.clientReferenceId = clientReferenceId;
            this.paymentIdFromMetadata = paymentIdFromMetadata;
        }
    }
}

