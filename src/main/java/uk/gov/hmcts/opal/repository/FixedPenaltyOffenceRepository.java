package uk.gov.hmcts.opal.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;

@Repository
public interface FixedPenaltyOffenceRepository extends JpaRepository<FixedPenaltyOffenceEntity, Long>,
    JpaSpecificationExecutor<FixedPenaltyOffenceEntity> {

    Optional<FixedPenaltyOffenceEntity> findByDefendantAccountId(Long defendantAccountId);


    void deleteByDefendantAccountId(long defendantAccountId);
}
