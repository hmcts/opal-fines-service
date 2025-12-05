package uk.gov.hmcts.opal.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity.Lite;

@Repository
public interface EnforcementRepository extends JpaRepository<Lite, Long>,
    JpaSpecificationExecutor<Lite> {

    void deleteByDefendantAccountId(long defendantAccountId);

    Optional<Lite> findFirstByDefendantAccountIdAndResultIdOrderByPostedDateDesc(
        long defendantAccountId, String resultId);

    List<Lite> findAllByDefendantAccountIdAndResultIdOrderByPostedDateDesc(
        long defendantAccountId, String resultId);
}
