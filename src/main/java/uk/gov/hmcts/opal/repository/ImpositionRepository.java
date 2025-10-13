package uk.gov.hmcts.opal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;

@Repository
public interface ImpositionRepository extends JpaRepository<ImpositionEntity.Lite, Long>,
    JpaSpecificationExecutor<ImpositionEntity.Lite> {
    List<ImpositionEntity.Lite> findAllByDefendantAccountId(long defendantAccountId);
}
