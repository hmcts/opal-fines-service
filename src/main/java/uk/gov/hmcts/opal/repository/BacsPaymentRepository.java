package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.BacsPaymentEntity;

@Repository
public interface BacsPaymentRepository extends JpaRepository<BacsPaymentEntity, Long>,
    JpaSpecificationExecutor<BacsPaymentEntity> {
}
