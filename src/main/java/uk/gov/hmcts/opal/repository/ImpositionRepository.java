package uk.gov.hmcts.opal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;

@Repository
public interface ImpositionRepository extends JpaRepository<ImpositionEntity, Long>,
    JpaSpecificationExecutor<ImpositionEntity> {

    @Override
    @EntityGraph(value = ImpositionEntity.ENTITY_GRAPH_FULL, type = EntityGraph.EntityGraphType.FETCH)
    Optional<ImpositionEntity> findById(Long impositionId);

    @Override
    @EntityGraph(value = ImpositionEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    List<ImpositionEntity> findAll(Specification<ImpositionEntity> spec);

    @EntityGraph(value = ImpositionEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    List<ImpositionEntity> findAllByDefendantAccountId(long defendantAccountId);
}
