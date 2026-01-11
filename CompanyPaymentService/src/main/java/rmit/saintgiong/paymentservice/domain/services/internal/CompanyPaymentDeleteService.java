package rmit.saintgiong.paymentservice.domain.services.internal;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rmit.saintgiong.paymentapi.internal.services.DeleteCompanyPaymentInterface;
import rmit.saintgiong.paymentservice.domain.repositories.CompanyPaymentRepository;

import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class CompanyPaymentDeleteService implements DeleteCompanyPaymentInterface {

    private final CompanyPaymentRepository repository;

    @Override
    @Transactional
    public void deleteCompanyPayment(String id) {
        log.info("method=deleteCompanyPayment, message=Start deleting payment, id={}", id);

        UUID uuid = UUID.fromString(id);
        repository.deleteById(uuid);

        log.info("method=deleteCompanyPayment, message=Successfully deleted payment, id={}", id);
    }
}
