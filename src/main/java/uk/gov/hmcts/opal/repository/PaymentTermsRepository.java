package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;

@Repository
public interface PaymentTermsRepository extends JpaRepository<PaymentTermsEntity, Long> {

    PaymentTermsEntity findByDefendantAccount_DefendantAccountId(DefendantAccountEntity defendantAccount);
}
