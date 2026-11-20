package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.SuspenseItemSummaryEntity;

@Repository
public interface SuspenseItemSummaryRepository  extends JpaRepository<SuspenseItemSummaryEntity, Long> {

    SuspenseItemSummaryEntity findByBusinessUnitIdAndSuspenseItemNumber(Long suspenseItemNumber, Short businessUnitId);
}
