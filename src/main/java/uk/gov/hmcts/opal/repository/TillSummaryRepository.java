package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.TillSummaryEntity;

@Repository
public interface TillSummaryRepository
    extends JpaRepository<TillSummaryEntity, Long>, JpaSpecificationExecutor<TillSummaryEntity> {
}
