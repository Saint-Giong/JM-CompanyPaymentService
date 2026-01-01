package rmit.saintgiong.paymentservice.domain.repositories.entities;

import jakarta.persistence.*;
import lombok.*;
import rmit.saintgiong.paymentapi.internal.common.type.PaymentMethod;
import rmit.saintgiong.paymentapi.internal.common.type.TransactionStatus;

import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "transaction", indexes = {
        @Index(name = "idx_transaction_payment_txn_id", columnList = "payment_transaction_id"),
        @Index(name = "idx_transaction_stripe_intent_id", columnList = "stripe_payment_intent_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyPaymentEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID companyId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String gateway;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Column(nullable = false)
    private LocalDateTime purchasedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    @Column(name = "payment_transaction_id", length = 255, unique = true)
    private String paymentTransactionId;

    @Column
    private UUID subscriptionId;
}
