package uk.gov.hmcts.opal.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;

@Repository
public interface EnforcementRepository extends JpaRepository<EnforcementEntity, Long>,
    JpaSpecificationExecutor<EnforcementEntity> {

    @Override
    @EntityGraph(value = EnforcementEntity.ENTITY_GRAPH_FULL, type = EntityGraph.EntityGraphType.FETCH)
    Optional<EnforcementEntity> findById(Long enforcementId);

    void deleteByDefendantAccountId(long defendantAccountId);

    @EntityGraph(value = EnforcementEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    Optional<EnforcementEntity> findFirstByDefendantAccountIdAndResultIdOrderByPostedDateDesc(
        Long defendantAccountId, String resultId);

    @EntityGraph(value = EnforcementEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    List<EnforcementEntity> findAllByDefendantAccountIdAndResultIdOrderByPostedDateDesc(
        Long defendantAccountId, String resultId);

    List<Lite> findByDefendantAccountIdIn(Set<Long> accountIds);

    Optional<Lite> findTopByDefendantAccountIdOrderByPostedDateDescEnforcementIdDesc(
        Long defendantAccountId
    );
}
