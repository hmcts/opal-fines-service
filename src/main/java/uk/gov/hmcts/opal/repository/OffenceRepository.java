package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.offence.OffenceEntity;

import java.util.Optional;
import java.util.function.Function;

@Repository
public interface OffenceRepository extends JpaRepository<OffenceEntity, Long>,
    JpaSpecificationExecutor<OffenceEntity> {

    @Query("""
    SELECT CASE WHEN COUNT(o) > 0 THEN TRUE ELSE FALSE END
    FROM OffenceEntity o
    WHERE o.offenceId = :offenceId
      AND (o.businessUnitId = :businessUnitId OR o.businessUnitId IS NULL)
    """)
    boolean existsByOffenceIdAvailableToBusinessUnit(
        @Param("offenceId") Long offenceId,
        @Param("businessUnitId") Short businessUnitId
    );

    @Override
    @EntityGraph(value = OffenceEntity.ENTITY_GRAPH_FULL, type = EntityGraph.EntityGraphType.FETCH)
    Optional<OffenceEntity> findById(Long offenceId);

    @Override
    @EntityGraph(value = OffenceEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    <S extends OffenceEntity, R> R findBy(
        Specification<OffenceEntity> spec,
        Function<? super JpaSpecificationExecutor.SpecificationFluentQuery<S>, R> queryFunction
    );
}
