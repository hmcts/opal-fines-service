package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.court.CourtEntity;

import java.util.Optional;
import java.util.function.Function;

@Repository
public interface CourtLiteRepository extends JpaRepository<CourtEntity, Long>,
    JpaSpecificationExecutor<CourtEntity> {

    @Override
    @EntityGraph(value = CourtEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    Optional<CourtEntity> findById(Long courtId);

    @Override
    @EntityGraph(value = CourtEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    <S extends CourtEntity, R> R findBy(
        Specification<CourtEntity> spec,
        Function<? super JpaSpecificationExecutor.SpecificationFluentQuery<S>, R> queryFunction
    );
}
