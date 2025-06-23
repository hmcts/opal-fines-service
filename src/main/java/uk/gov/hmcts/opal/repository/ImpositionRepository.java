package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.ImpositionEntity;

@Repository
public interface ImpositionRepository extends JpaRepository<ImpositionEntity, Long>,
    JpaSpecificationExecutor<ImpositionEntity> {
    void deleteByDefendantAccount_DefendantAccountId(long defendantAccountId);
}
