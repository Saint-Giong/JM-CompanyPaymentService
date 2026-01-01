package rmit.saintgiong.paymentservice.domain.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rmit.saintgiong.paymentapi.internal.common.dto.response.QueryCompanyPaymentResponseDto;
import rmit.saintgiong.paymentapi.internal.services.QueryCompanyPaymentInterface;
import rmit.saintgiong.paymentservice.domain.mappers.CompanyPaymentMapper;
import rmit.saintgiong.paymentservice.domain.repositories.CompanyPaymentRepository;
import rmit.saintgiong.paymentservice.domain.repositories.entities.CompanyPaymentEntity;
import rmit.saintgiong.profileapi.internal.common.type.DomainCode;
import rmit.saintgiong.profileservice.common.exception.DomainException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static rmit.saintgiong.profileapi.internal.common.type.DomainCode.RESOURCE_NOT_FOUND;

@Service
@AllArgsConstructor
@Slf4j
public class CompanyPaymentQueryService implements QueryCompanyPaymentInterface {

    private final CompanyPaymentMapper mapper;

    private final CompanyPaymentRepository repository;

    @Override
    @Transactional(readOnly = true)
    public QueryCompanyPaymentResponseDto getCompanyPayment(String id) {
        log.info("method=getCompanyPayment, message=Start fetching payment, id={}", id);

        UUID uuid = UUID.fromString(id);
        CompanyPaymentEntity existing = repository.findById(uuid)
                .orElseThrow(() -> new DomainException(RESOURCE_NOT_FOUND, "Transaction not found"));

        QueryCompanyPaymentResponseDto response = mapper.toQueryResponse(existing);
        response.setId(String.valueOf(existing.getId()));
        response.setCompanyId(String.valueOf(existing.getCompanyId()));

        log.info("method=getCompanyPayment, message=Successfully fetched payment, id={}", id);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<QueryCompanyPaymentResponseDto> listCompanyPayments() {
        log.info("method=listCompanyPayments, message=Start listing payments");

        List<QueryCompanyPaymentResponseDto> items = repository.findAll().stream()
                .map(entity -> {
                    QueryCompanyPaymentResponseDto dto = mapper.toQueryResponse(entity);
                    dto.setId(String.valueOf(entity.getId()));
                    dto.setCompanyId(String.valueOf(entity.getCompanyId()));
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("method=listCompanyPayments, message=Successfully listed payments, count={}", items.size());
        return items;
    }
}
