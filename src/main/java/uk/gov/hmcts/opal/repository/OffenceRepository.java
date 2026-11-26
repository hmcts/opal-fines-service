package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.offence.OffenceLegacyEntity;

import java.util.Optional;
import java.util.function.Function;

@Repository
public interface OffenceRepository extends JpaRepository<OffenceLegacyEntity, Long>,
    JpaSpecificationExecutor<OffenceLegacyEntity> {

    default boolean existsByOffenceIdAvailableToBusinessUnit(Long offenceId, Short businessUnitId) {
        return existsByOffenceIdAndBusinessUnitIdOrOffenceIdAndBusinessUnitIdIsNull(
            offenceId,
            businessUnitId,
            offenceId
        );
    }

    boolean existsByOffenceIdAndBusinessUnitIdOrOffenceIdAndBusinessUnitIdIsNull(
        Long offenceId,
        Short businessUnitId,
        Long globalOffenceId
    );

    @Override
    @EntityGraph(value = OffenceLegacyEntity.ENTITY_GRAPH_FULL, type = EntityGraph.EntityGraphType.FETCH)
    Optional<OffenceLegacyEntity> findById(Long offenceId);

    @Override
    @EntityGraph(value = OffenceLegacyEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    <S extends OffenceLegacyEntity, R> R findBy(
        Specification<OffenceLegacyEntity> spec,
        Function<? super JpaSpecificationExecutor.SpecificationFluentQuery<S>, R> queryFunction
    );
}
