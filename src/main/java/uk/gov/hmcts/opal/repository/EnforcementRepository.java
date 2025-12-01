package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.EnforcementEntity;

@Repository
public interface EnforcementRepository extends JpaRepository<EnforcementEntity, Long>,
    JpaSpecificationExecutor<EnforcementEntity> {

    void deleteByDefendantAccount_DefendantAccountId(long defendantAccountId);
}
