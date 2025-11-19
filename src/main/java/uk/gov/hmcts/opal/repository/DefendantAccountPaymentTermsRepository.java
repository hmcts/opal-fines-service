package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface DefendantAccountPaymentTermsRepository extends JpaRepository<PaymentTermsEntity, Long>,
    JpaSpecificationExecutor<PaymentTermsEntity> {

    Optional<PaymentTermsEntity>
        findTopByDefendantAccount_DefendantAccountIdOrderByPostedDateDescPaymentTermsIdDesc(Long accountId);

    List<PaymentTermsEntity> findByDefendantAccount_DefendantAccountIdAndActiveTrue(Long defendantAccountId);
}
