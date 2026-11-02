package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.court.CourtLegacyEntity;

import java.util.Optional;
import java.util.function.Function;

@Repository
public interface CourtRepository extends JpaRepository<CourtLegacyEntity, Long>,
    JpaSpecificationExecutor<CourtLegacyEntity> {

    @Override
    @EntityGraph(value = CourtLegacyEntity.ENTITY_GRAPH_FULL, type = EntityGraph.EntityGraphType.FETCH)
    Optional<CourtLegacyEntity> findById(Long courtId);

    @Override
    @EntityGraph(value = CourtLegacyEntity.ENTITY_GRAPH_FULL, type = EntityGraph.EntityGraphType.FETCH)
    <S extends CourtLegacyEntity, R> R findBy(
        Specification<CourtLegacyEntity> spec,
        Function<? super JpaSpecificationExecutor.SpecificationFluentQuery<S>, R> queryFunction
    );

    Optional<CourtLegacyEntity> findByCourtCodeAndBusinessUnitId(Short courtCode, Short businessUnitId);
}
