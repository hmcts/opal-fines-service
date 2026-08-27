package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.LegacyLocalJusticeAreaEntity;

@Repository
public interface LegacyLocalJusticeAreaRepository extends
    ViewRepository<LegacyLocalJusticeAreaEntity, Short>,
    JpaSpecificationExecutor<LegacyLocalJusticeAreaEntity> {
}
