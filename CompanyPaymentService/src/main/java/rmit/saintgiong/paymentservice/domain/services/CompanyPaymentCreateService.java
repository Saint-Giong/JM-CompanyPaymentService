package rmit.saintgiong.paymentservice.domain.services;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rmit.saintgiong.paymentapi.internal.common.dto.request.CreateCompanyPaymentRequestDto;
import rmit.saintgiong.paymentapi.internal.common.dto.response.CreateCompanyPaymentResponseDto;
import rmit.saintgiong.paymentapi.internal.common.type.TransactionStatus;
import rmit.saintgiong.paymentapi.internal.services.CreateCompanyPaymentInterface;
import rmit.saintgiong.paymentservice.domain.repositories.CompanyPaymentRepository;
import rmit.saintgiong.paymentservice.domain.repositories.entities.CompanyPaymentEntity;
import rmit.saintgiong.paymentservice.service.StripePaymentService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyPaymentCreateService implements CreateCompanyPaymentInterface {

    private final CompanyPaymentRepository repository;

    private final StripePaymentService stripePaymentService;

    @Value("${stripe.checkout.successUrl:}")
    private String successUrl;

    @Value("${stripe.checkout.cancelUrl:}")
    private String cancelUrl;

    @Override
    @Transactional
    public CreateCompanyPaymentResponseDto createCompanyPayment(CreateCompanyPaymentRequestDto req) {
        log.info("method=createCompanyPayment, message=Start creating company payment (Stripe Checkout), req={}", req);

        UUID companyId = UUID.fromString(req.getCompanyId());
        UUID paymentId = UUID.randomUUID();

        CompanyPaymentEntity entity = CompanyPaymentEntity.builder()
                .id(paymentId)
                .companyId(companyId)
                .amount(req.getAmount())
                .currency(req.getCurrency())
                .gateway("stripe")
                .method(req.getMethod())
                .purchasedAt(LocalDateTime.now())
                .status(TransactionStatus.PENDING)
                .build();

        Map<String, String> metadata = new HashMap<>();
        metadata.put("paymentId", paymentId.toString());
        metadata.put("companyId", companyId.toString());

        StripePaymentService.CheckoutSessionResult session = stripePaymentService.createCheckoutSession(
                req.getAmount(),
                req.getCurrency(),
                successUrl,
                cancelUrl,
                "Company payment",
                paymentId.toString(),
                metadata
        );

        entity.setStripeCheckoutSessionId(session.getId());
        if (session.getPaymentIntentId() != null && !session.getPaymentIntentId().isBlank()) {
            entity.setStripePaymentIntentId(session.getPaymentIntentId());
        }

        CompanyPaymentEntity saved = repository.save(entity);

        log.info("method=createCompanyPayment, message=Created company payment PENDING, id={}, checkoutSessionId={}", saved.getId(), saved.getStripeCheckoutSessionId());

        return CreateCompanyPaymentResponseDto.builder()
                .id(String.valueOf(saved.getId()))
                .status(saved.getStatus())
                .stripeCheckoutSessionId(saved.getStripeCheckoutSessionId())
                .checkoutUrl(session.getUrl())
                .build();
    }
}
