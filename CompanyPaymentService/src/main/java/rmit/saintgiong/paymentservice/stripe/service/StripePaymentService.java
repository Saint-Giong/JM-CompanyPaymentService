package rmit.saintgiong.paymentservice.stripe.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Map;

@Component
@Slf4j
public class StripePaymentService {

    /**
     * Creates a Stripe PaymentIntent.
     * Stripe expects amount in the smallest currency unit (e.g., cents) as a long.
     * We accept Double and convert using 2 decimal places (USD-style). If you need
     * per-currency exponent handling (JPY=0, etc.), we can extend this.
     */
    public String createPaymentIntent(Double amount, String currency) {
        validateAmountCurrency(amount, currency);

        long minorUnits = toMinorUnits(amount);

        log.info("createPaymentIntent, amount={}, currentcy={}", amount, currency);

        try {
            PaymentIntentCreateParams.Builder params = PaymentIntentCreateParams.builder()
                    .setAmount(minorUnits)
                    .setCurrency(currency.trim().toLowerCase(Locale.ROOT));

            PaymentIntent intent = PaymentIntent.create(params.build());
            return intent.getId();
        } catch (StripeException e) {
            throw new RuntimeException("Stripe API error", e);
        }
    }

    /**
     * Creates a Stripe Checkout Session (hosted checkout page) and returns its id + url.
     */
    public CheckoutSessionResult createCheckoutSession(
            Double amount,
            String currency,
            String successUrl,
            String cancelUrl,
            String description,
            String clientReferenceId,
            Map<String, String> metadata
    ) {
        validateAmountCurrency(amount, currency);
        if (successUrl == null || successUrl.isBlank()) {
            throw new IllegalArgumentException("successUrl must not be blank");
        }
        if (cancelUrl == null || cancelUrl.isBlank()) {
            throw new IllegalArgumentException("cancelUrl must not be blank");
        }

        long minorUnits = toMinorUnits(amount);
        String normalizedCurrency = currency.trim().toLowerCase(Locale.ROOT);

        try {
            SessionCreateParams.LineItem.PriceData.ProductData productData =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName((description == null || description.isBlank()) ? "Company payment" : description)
                            .build();

            SessionCreateParams.LineItem.PriceData priceData =
                    SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(normalizedCurrency)
                            .setUnitAmount(minorUnits)
                            .setProductData(productData)
                            .build();

            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(priceData)
                    .build();

            SessionCreateParams.Builder params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .addLineItem(lineItem);

            if (clientReferenceId != null && !clientReferenceId.isBlank()) {
                params.setClientReferenceId(clientReferenceId);
            }
            if (metadata != null && !metadata.isEmpty()) {
                params.putAllMetadata(metadata);
            }

            Session session = Session.create(params.build());

            return new CheckoutSessionResult(session.getId(), session.getUrl(), session.getPaymentIntent());
        } catch (StripeException e) {
            throw new RuntimeException("Stripe API error", e);
        }
    }

    @Data
    @AllArgsConstructor
    public static class CheckoutSessionResult {
        private String id;
        private String url;
        /** PaymentIntent id if created by Checkout (often available immediately). */
        private String paymentIntentId;
    }

    private void validateAmountCurrency(Double amount, String currency) {
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null");
        }
        if (amount <= 0d) {
            throw new IllegalArgumentException("amount must be > 0");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("currency must not be blank");
        }
    }

    private long toMinorUnits(Double amount) {
        // Convert with exact decimal handling and round to 2 decimal places.
        BigDecimal bd = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
        return bd.movePointRight(2).longValueExact();
    }
}
