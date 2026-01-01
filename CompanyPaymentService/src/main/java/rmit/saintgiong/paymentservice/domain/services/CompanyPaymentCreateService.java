package rmit.saintgiong.paymentservice.domain.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class CompanyPaymentCreateService implements CreateCompanyPaymentInterface {

    private final CompanyPaymentRepository repository;

    private final StripePaymentService stripePaymentService;

    @Override
    @Transactional
    public CreateCompanyPaymentResponseDto createCompanyPayment(CreateCompanyPaymentRequestDto req) {
        log.info("method=createCompanyPayment, message=Start creating company payment, req={}", req);

        UUID companyId = UUID.fromString(req.getCompanyId());

        CompanyPaymentEntity entity = CompanyPaymentEntity.builder()
                .id(UUID.randomUUID())
                .companyId(companyId)
                .amount(req.getAmount())
                .currency(req.getCurrency())
                .gateway("stripe")
                .method(req.getMethod())
                .purchasedAt(LocalDateTime.now())
                .status(TransactionStatus.PENDING)
                .build();

        // Stripe PaymentIntent
        String paymentIntentId = stripePaymentService.createPaymentIntent(req.getAmount(), req.getCurrency());
        entity.setStripePaymentIntentId(paymentIntentId);
        entity.setStatus(TransactionStatus.SUCCESSFUL);

        CompanyPaymentEntity saved = repository.save(entity);

        log.info("method=createCompanyPayment, message=Successfully created company payment, id={}", saved.getId());

        return CreateCompanyPaymentResponseDto.builder()
                .id(String.valueOf(saved.getId()))
                .status(saved.getStatus())
                .build();
    }
}
