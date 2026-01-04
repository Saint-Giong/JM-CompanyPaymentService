package rmit.saintgiong.paymentservice.domain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rmit.saintgiong.paymentservice.domain.repositories.entities.CompanyPaymentEntity;

import java.util.Optional;
import java.util.UUID;

public interface CompanyPaymentRepository extends JpaRepository<CompanyPaymentEntity, UUID> {
    Optional<CompanyPaymentEntity> findByStripePaymentIntentId(String stripePaymentIntentId);
    Optional<CompanyPaymentEntity> findByStripeCheckoutSessionId(String stripeCheckoutSessionId);
    Optional<CompanyPaymentEntity> findByPaymentTransactionId(String paymentTransactionId);
}
