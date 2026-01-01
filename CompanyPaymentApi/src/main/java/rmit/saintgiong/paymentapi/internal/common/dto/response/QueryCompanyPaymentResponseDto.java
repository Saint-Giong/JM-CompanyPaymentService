package rmit.saintgiong.paymentapi.internal.common.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import rmit.saintgiong.paymentapi.internal.common.type.TransactionStatus;
import rmit.saintgiong.paymentapi.internal.common.type.PaymentMethod;

@Data
@Builder
public class QueryCompanyPaymentResponseDto {
    private String id;
    private String companyId;
    private Long amount;
    private String currency;
    private TransactionStatus status;
    private PaymentMethod method;
    private String paymentTransactionId;
    private String stripePaymentIntentId;
    private String description;
    private Instant createdAt;
}
