package uk.gov.hmcts.opal.repository;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementAccountTypeEntity;

@Repository
public interface EnforcementAccountTypeRepository extends ViewRepository<EnforcementAccountTypeEntity, Long> {
}
