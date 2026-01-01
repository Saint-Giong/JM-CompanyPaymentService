package rmit.saintgiong.paymentservice.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

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
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null");
        }
        if (amount <= 0d) {
            throw new IllegalArgumentException("amount must be > 0");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("currency must not be blank");
        }

        long minorUnits = toMinorUnits(amount);

        log.info("createPaymentIntent, amount={}, currentcy={}",amount, currency);

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

    private long toMinorUnits(Double amount) {
        // Convert with exact decimal handling and round to 2 decimal places.
        BigDecimal bd = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
        return bd.movePointRight(2).longValueExact();
    }
}
