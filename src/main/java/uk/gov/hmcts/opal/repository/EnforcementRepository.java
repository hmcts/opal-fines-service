package uk.gov.hmcts.opal.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;

@Repository
public interface EnforcementRepository extends JpaRepository<EnforcementEntity, Long>,
    JpaSpecificationExecutor<EnforcementEntity> {

    @Override
    @EntityGraph(value = EnforcementEntity.ENTITY_GRAPH_FULL, type = EntityGraph.EntityGraphType.FETCH)
    Optional<EnforcementEntity> findById(Long enforcementId);

    @Override
    @EntityGraph(value = EnforcementEntity.ENTITY_GRAPH_FULL, type = EntityGraph.EntityGraphType.FETCH)
    List<EnforcementEntity> findAll(Specification<EnforcementEntity> spec);

    void deleteByDefendantAccountId(long defendantAccountId);

    @EntityGraph(value = EnforcementEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    Optional<EnforcementEntity> findFirstByDefendantAccountIdAndResultIdOrderByPostedDateDesc(
        Long defendantAccountId, String resultId);

    @EntityGraph(value = EnforcementEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    EnforcementEntity findTopByDefendantAccountIdAndResultIdOrderByPostedDateAsc(
        Long defendantAccountId, String resultId);

    @EntityGraph(value = EnforcementEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    Optional<EnforcementEntity> findTopByDefendantAccountIdOrderByPostedDateDescEnforcementIdDesc(
        Long defendantAccountId
    );

    @Query("""
        SELECT e
        FROM EnforcementEntity e
        WHERE e.defendantAccountId = :defendantAccountId
        ORDER BY e.postedDate DESC, e.enforcementId DESC
        """)
    List<EnforcementEntity> findHistoryRowsByDefendantAccountId(@Param("defendantAccountId") Long defendantAccountId);

    @EntityGraph(value = EnforcementEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    List<EnforcementEntity> findByDefendantAccountIdAndResultId(Long accountId, String regf);

    @EntityGraph(value = EnforcementEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    EnforcementEntity findTopByDefendantAccountIdAndResultIdOrderByPostedDateDescResultIdDesc(
        Long defendantAccountId, String resultId);
}
