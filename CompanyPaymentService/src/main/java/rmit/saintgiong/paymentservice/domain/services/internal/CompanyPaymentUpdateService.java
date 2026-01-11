package rmit.saintgiong.paymentservice.domain.services.internal;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rmit.saintgiong.paymentapi.internal.common.dto.request.CreateCompanyPaymentRequestDto;
import rmit.saintgiong.paymentapi.internal.common.dto.response.QueryCompanyPaymentResponseDto;
import rmit.saintgiong.paymentapi.internal.common.type.PaymentMethod;
import rmit.saintgiong.paymentapi.internal.services.UpdateCompanyPaymentInterface;
import rmit.saintgiong.paymentservice.domain.mappers.CompanyPaymentMapper;
import rmit.saintgiong.paymentservice.domain.repositories.CompanyPaymentRepository;
import rmit.saintgiong.paymentservice.domain.repositories.entities.CompanyPaymentEntity;

import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class CompanyPaymentUpdateService implements UpdateCompanyPaymentInterface {

    private final CompanyPaymentRepository repository;

    private final CompanyPaymentMapper mapper;

    @Override
    @Transactional
    public QueryCompanyPaymentResponseDto updateCompanyPayment(String id, CreateCompanyPaymentRequestDto req) {
        log.info("method=updateCompanyPayment, message=Start updating payment, id={}, req={}", id, req);

        UUID uuid = UUID.fromString(id);
        CompanyPaymentEntity existing = repository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));

        if (req.getAmount() != null) existing.setAmount(req.getAmount());
        if (req.getCurrency() != null) existing.setCurrency(req.getCurrency());
        if (req.getMethod() != null) {
            existing.setMethod(PaymentMethod.valueOf(req.getMethod().name()));
        }

        CompanyPaymentEntity saved = repository.save(existing);

        QueryCompanyPaymentResponseDto response = mapper.toQueryResponse(saved);
        response.setId(String.valueOf(saved.getId()));
        response.setCompanyId(String.valueOf(saved.getCompanyId()));

        log.info("method=updateCompanyPayment, message=Successfully updated payment, id={}", id);
        return response;
    }
}
