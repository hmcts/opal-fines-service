package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;

@Repository
public interface PaymentTermsRepository extends JpaRepository<PaymentTermsEntity, Long>,
    JpaSpecificationExecutor<PaymentTermsEntity> {

    PaymentTermsEntity findByDefendantAccount_DefendantAccountId(Long defendantAccountId);

    void deleteByDefendantAccount_DefendantAccountId(long defendantAccountId);
}
