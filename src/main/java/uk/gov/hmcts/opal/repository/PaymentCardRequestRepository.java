package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.opal.entity.PaymentCardRequestEntity;

public interface PaymentCardRequestRepository extends JpaRepository<PaymentCardRequestEntity, Long> {

    void deleteByDefendantAccountId(long defendantAccountId);
}
