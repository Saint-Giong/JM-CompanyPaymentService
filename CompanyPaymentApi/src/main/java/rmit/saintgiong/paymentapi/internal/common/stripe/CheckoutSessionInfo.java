package rmit.saintgiong.paymentapi.internal.common.stripe;


public record CheckoutSessionInfo(
        String sessionId,
        String paymentStatus,
        String paymentIntentId,
        String clientReferenceId,
        String paymentIdFromMetadata) {
}
