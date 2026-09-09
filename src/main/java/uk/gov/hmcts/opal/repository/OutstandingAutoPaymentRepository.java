package uk.gov.hmcts.opal.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.businessunit.OutstandingAutoPaymentEntity;

@Repository
public interface OutstandingAutoPaymentRepository extends JpaRepository<OutstandingAutoPaymentEntity, Short> {

    List<OutstandingAutoPaymentEntity> findByBusinessUnitIdInOrderByBusinessUnitNameAsc(
        List<Short> businessUnitIds);
}
