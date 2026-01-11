package rmit.saintgiong.paymentservice.domain.controllers;

import com.stripe.exception.SignatureVerificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rmit.saintgiong.paymentapi.external.services.stripe.ExternalStripeCheckoutInterface;
import rmit.saintgiong.paymentapi.external.services.stripe.ExternalStripeWebhookInterface;
import rmit.saintgiong.paymentapi.internal.common.dto.request.CreateStripeCheckoutRequestDto;
import rmit.saintgiong.paymentapi.internal.common.dto.response.CreateStripeCheckoutResponseDto;

@RestController
@RequestMapping("/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeController {

    private final ExternalStripeCheckoutInterface externalStripeCheckoutInterface;
    private final ExternalStripeWebhookInterface externalStripeWebhookInterface;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader
    ) {
        try {
            externalStripeWebhookInterface.handleWebhook(payload, sigHeader);
            return ResponseEntity.ok("Webhook processed successfully");

        } catch (IllegalArgumentException e) {
            log.warn("method=handleWebhook, message=Webhook validation failed: {}", e.getMessage());

            if (e.getMessage().contains("not configured")) {
                return ResponseEntity.ok("Webhook ignored - not configured");
            }

            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (SignatureVerificationException e) {
            log.warn("method=handleWebhook, message=Invalid webhook signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");

        } catch (Exception e) {
            log.error("method=handleWebhook, message=Unexpected error processing webhook: {}", e.getMessage(), e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing webhook");
        }
    }

    @PostMapping("/checkout-session")
    public ResponseEntity<CreateStripeCheckoutResponseDto> createCheckoutSession(
            @RequestBody CreateStripeCheckoutRequestDto request
    ) {
        CreateStripeCheckoutResponseDto response = externalStripeCheckoutInterface
                .createStripeCheckout(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}

