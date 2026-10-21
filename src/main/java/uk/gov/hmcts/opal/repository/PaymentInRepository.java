package uk.gov.hmcts.opal.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import java.util.Collection;

@Repository
public interface PaymentInRepository extends JpaRepository<PaymentInEntity, Long>,
    JpaSpecificationExecutor<PaymentInEntity> {

    List<PaymentInEntity> findByTillEntity_TillIdOrderByPaymentDateAscPaymentInIdAsc(Long tillId);

    void deleteByTillEntity_TillIdIn(Collection<Long> tillIds);
}
