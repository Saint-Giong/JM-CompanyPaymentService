package rmit.saintgiong.paymentservice.domain.services.external.payment;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rmit.saintgiong.paymentapi.external.services.ExternalCompanyPaymentRequestInterface;
import rmit.saintgiong.paymentapi.external.services.kafka.EventProducerInterface;
import rmit.saintgiong.paymentapi.internal.common.type.KafkaTopic;
import rmit.saintgiong.shared.dto.avro.payment.SubscriptionPaidRequestRecord;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExternalCompanyPaymentRequestService implements ExternalCompanyPaymentRequestInterface {

    private final EventProducerInterface eventProducer;

    @Override
    public void sendSubscriptionPaidRequest(UUID companyId, UUID transactionId, String status) {
        log.info("method=sendSubscriptionPaidNotification, message=Sending subscription paid notification, companyId={}, transactionId={}, status={}",
                companyId, transactionId, status);

        SubscriptionPaidRequestRecord requestRecord = SubscriptionPaidRequestRecord.newBuilder()
                .setCompanyId(companyId)
                .setTransactionId(transactionId)
                .setStatus(status)
                .build();

        eventProducer.send(
                KafkaTopic.SUBSCRIPTION_PAID_NOTIFICATION_TOPIC,
                requestRecord
        );

        log.info("method=sendSubscriptionPaidNotification, message=Subscription paid notification sent successfully, companyId={}, status={}", 
                companyId, status);
    }
}
