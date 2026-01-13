package rmit.saintgiong.paymentservice.common.config;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rmit.saintgiong.paymentapi.internal.common.type.PaymentMethod;
import rmit.saintgiong.paymentapi.internal.common.type.TransactionStatus;
import rmit.saintgiong.paymentservice.domain.repositories.CompanyPaymentRepository;
import rmit.saintgiong.paymentservice.domain.repositories.entities.CompanyPaymentEntity;


@Component
@Slf4j
@RequiredArgsConstructor
public class DataSeedingConfig implements CommandLineRunner {

    private final CompanyPaymentRepository companyPaymentRepository;

    // Company UUIDs - must match Auth service
    private static final UUID NAB_COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID GOOGLE_COMPANY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID NETCOMPANY_COMPANY_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID SHOPEE_COMPANY_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    // Subscription UUIDs - must match Subscription service
    private static final UUID NAB_SUBSCRIPTION_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID GOOGLE_SUBSCRIPTION_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID NETCOMPANY_SUBSCRIPTION_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID SHOPEE_SUBSCRIPTION_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

    // Transaction UUIDs - must match Subscription service transactionId
    private static final UUID NAB_TRANSACTION_ID = UUID.fromString("11111111-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID GOOGLE_TRANSACTION_ID = UUID.fromString("22222222-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID NETCOMPANY_TRANSACTION_ID = UUID.fromString("33333333-cccc-cccc-cccc-cccccccccccc");
    private static final UUID SHOPEE_TRANSACTION_ID = UUID.fromString("44444444-dddd-dddd-dddd-dddddddddddd");

    // Subscription price (monthly)
    private static final Double SUBSCRIPTION_PRICE = 29.99;

    @Override
    public void run(String @NonNull ... args) {
        if (companyPaymentRepository.count() != 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        List<CompanyPaymentEntity> seeds = List.of(
                // ============================================================
                // FREEMIUM 1: NAB - Payment for EXPIRED subscription
                // Purchased when subscription started (now - 31 days - 30 days = now - 61 days ago)
                // This was the last successful payment before subscription expired
                // ============================================================
                CompanyPaymentEntity.builder()
                        .id(NAB_TRANSACTION_ID)
                        .companyId(NAB_COMPANY_ID)
                        .amount(SUBSCRIPTION_PRICE)
                        .currency("USD")
                        .gateway("Stripe")
                        .method(PaymentMethod.CREDIT_CARD)
                        .purchasedAt(now.minusDays(61)) // 61 days ago (31 days expired + 30 days subscription period)
                        .status(TransactionStatus.SUCCESSFUL)
                        .stripePaymentIntentId("pi_nab_" + NAB_TRANSACTION_ID.toString().substring(0, 8))
                        .stripeCheckoutSessionId("cs_nab_" + NAB_TRANSACTION_ID.toString().substring(0, 8))
                        .paymentTransactionId("txn_nab_" + NAB_TRANSACTION_ID.toString().substring(0, 8))
                        .subscriptionId(NAB_SUBSCRIPTION_ID)
                        .build(),

                // ============================================================
                // FREEMIUM 2: Google - Payment for CANCELLED subscription
                // Transaction was 50 days ago (per requirement)
                // Logic: if EXPIRED > 3 days, status becomes CANCELLED with null expiry
                // ============================================================
                CompanyPaymentEntity.builder()
                        .id(GOOGLE_TRANSACTION_ID)
                        .companyId(GOOGLE_COMPANY_ID)
                        .amount(SUBSCRIPTION_PRICE)
                        .currency("USD")
                        .gateway("Stripe")
                        .method(PaymentMethod.VISA)
                        .purchasedAt(now.minusDays(50)) // 50 days ago as per requirement
                        .status(TransactionStatus.SUCCESSFUL)
                        .stripePaymentIntentId("pi_google_" + GOOGLE_TRANSACTION_ID.toString().substring(0, 8))
                        .stripeCheckoutSessionId("cs_google_" + GOOGLE_TRANSACTION_ID.toString().substring(0, 8))
                        .paymentTransactionId("txn_google_" + GOOGLE_TRANSACTION_ID.toString().substring(0, 8))
                        .subscriptionId(GOOGLE_SUBSCRIPTION_ID)
                        .build(),

                // ============================================================
                // PREMIUM 1: Netcompany - Payment for ACTIVE subscription
                // Purchased recently (subscription active for 30 more days)
                // ============================================================
                CompanyPaymentEntity.builder()
                        .id(NETCOMPANY_TRANSACTION_ID)
                        .companyId(NETCOMPANY_COMPANY_ID)
                        .amount(SUBSCRIPTION_PRICE)
                        .currency("USD")
                        .gateway("Stripe")
                        .method(PaymentMethod.CREDIT_CARD)
                        .purchasedAt(now) // Just purchased
                        .status(TransactionStatus.SUCCESSFUL)
                        .stripePaymentIntentId("pi_netcompany_" + NETCOMPANY_TRANSACTION_ID.toString().substring(0, 8))
                        .stripeCheckoutSessionId("cs_netcompany_" + NETCOMPANY_TRANSACTION_ID.toString().substring(0, 8))
                        .paymentTransactionId("txn_netcompany_" + NETCOMPANY_TRANSACTION_ID.toString().substring(0, 8))
                        .subscriptionId(NETCOMPANY_SUBSCRIPTION_ID)
                        .build(),

                // ============================================================
                // PREMIUM 2: Shopee - Payment for ACTIVE subscription
                // Purchased recently (subscription active for 30 more days)
                // ============================================================
                CompanyPaymentEntity.builder()
                        .id(SHOPEE_TRANSACTION_ID)
                        .companyId(SHOPEE_COMPANY_ID)
                        .amount(SUBSCRIPTION_PRICE)
                        .currency("USD")
                        .gateway("Stripe")
                        .method(PaymentMethod.E_WALLET)
                        .purchasedAt(now) // Just purchased
                        .status(TransactionStatus.SUCCESSFUL)
                        .stripePaymentIntentId("pi_shopee_" + SHOPEE_TRANSACTION_ID.toString().substring(0, 8))
                        .stripeCheckoutSessionId("cs_shopee_" + SHOPEE_TRANSACTION_ID.toString().substring(0, 8))
                        .paymentTransactionId("txn_shopee_" + SHOPEE_TRANSACTION_ID.toString().substring(0, 8))
                        .subscriptionId(SHOPEE_SUBSCRIPTION_ID)
                        .build()
        );

        companyPaymentRepository.saveAll(seeds);
        log.info("Seeded {} payment transactions (linked to subscriptions).", seeds.size());
    }
}
