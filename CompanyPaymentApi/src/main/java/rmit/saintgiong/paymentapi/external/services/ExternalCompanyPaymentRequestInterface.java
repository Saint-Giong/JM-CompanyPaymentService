package rmit.saintgiong.paymentapi.external.services;

import java.util.UUID;

public interface ExternalCompanyPaymentRequestInterface {

    void sendSubscriptionPaidRequest(UUID companyId, UUID transactionId, String status);
}
