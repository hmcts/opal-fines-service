package uk.gov.hmcts.opal.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.PaymentInEntity;

@Repository
public interface PaymentInRepository extends JpaRepository<PaymentInEntity, Long> {

    List<PaymentInEntity> findByTillEntity_TillIdOrderByPaymentDateAscPaymentInIdAsc(Long tillId);
}
