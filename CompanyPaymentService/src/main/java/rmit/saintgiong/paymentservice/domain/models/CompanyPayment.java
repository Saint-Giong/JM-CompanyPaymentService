package rmit.saintgiong.paymentservice.domain.models;

import lombok.*;
import rmit.saintgiong.paymentapi.internal.common.type.PaymentMethod;
import rmit.saintgiong.paymentapi.internal.common.type.TransactionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyPayment {
    private UUID id;
    private UUID companyId;
    private Double amount;
    private String currency;
    private String gateway;
    private PaymentMethod method;
    private String paymentTransactionId;
    private LocalDateTime purchasedAt;
    private TransactionStatus status;
    private String stripePaymentIntentId;
    private String description;
    private UUID accountId;
    private UUID subscriptionId;
}

