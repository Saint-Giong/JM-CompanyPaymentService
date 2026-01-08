package rmit.saintgiong.paymentservice.domain.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rmit.saintgiong.paymentapi.internal.common.dto.request.CreateStripeCheckoutRequestDto;
import rmit.saintgiong.paymentapi.internal.common.dto.response.CreateStripeCheckoutResponseDto;
import rmit.saintgiong.paymentapi.internal.services.CreateStripeCheckoutInterface;
import rmit.saintgiong.paymentservice.stripe.service.StripePaymentService;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeCheckoutService implements CreateStripeCheckoutInterface {

    private final StripePaymentService stripePaymentService;

    @Override
    public CreateStripeCheckoutResponseDto createStripeCheckout(CreateStripeCheckoutRequestDto req) {
        log.info("method=createStripeCheckout, message=Creating Stripe checkout session (no DB save), amount={}, currency={}", 
                req.getAmount(), req.getCurrency());

        // Create Stripe checkout session
        StripePaymentService.CheckoutSessionResult result = stripePaymentService.createCheckoutSession(
            req.getAmount(),
            req.getCurrency(),
            req.getSuccessUrl(),
            req.getCancelUrl(),
            req.getDescription() != null ? req.getDescription() : "Payment",
            null, // clientReferenceId - not needed for JA
            req.getMetadata()
        );

        log.info("method=createStripeCheckout, message=Stripe checkout session created, sessionId={}, url={}", 
                result.getId(), result.getUrl());

        return CreateStripeCheckoutResponseDto.builder()
                .sessionId(result.getId())
                .checkoutUrl(result.getUrl())
                .paymentIntentId(result.getPaymentIntentId())
                .build();
    }
}
