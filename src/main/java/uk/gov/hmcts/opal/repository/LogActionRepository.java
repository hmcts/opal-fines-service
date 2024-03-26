package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.LogActionEntity;

@Repository
public interface LogActionRepository extends JpaRepository<LogActionEntity, Short>,
    JpaSpecificationExecutor<LogActionEntity> {
}
