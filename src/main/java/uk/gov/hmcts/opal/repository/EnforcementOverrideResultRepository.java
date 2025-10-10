package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.opal.entity.EnforcementOverrideResultEntity;

public interface EnforcementOverrideResultRepository
    extends JpaRepository<EnforcementOverrideResultEntity, String> {
}
