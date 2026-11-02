package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorLegacyEntity;

import java.util.Optional;
import java.util.function.Function;

@Repository
public interface MajorCreditorRepository extends JpaRepository<MajorCreditorLegacyEntity, Long>,
    JpaSpecificationExecutor<MajorCreditorLegacyEntity> {

    @Override
    @EntityGraph(value = MajorCreditorLegacyEntity.ENTITY_GRAPH_FULL, type = EntityGraph.EntityGraphType.FETCH)
    Optional<MajorCreditorLegacyEntity> findById(Long majorCreditorId);

    @Override
    @EntityGraph(value = MajorCreditorLegacyEntity.ENTITY_GRAPH_FULL, type = EntityGraph.EntityGraphType.FETCH)
    <S extends MajorCreditorLegacyEntity, R> R findBy(
        Specification<MajorCreditorLegacyEntity> spec,
        Function<? super JpaSpecificationExecutor.SpecificationFluentQuery<S>, R> queryFunction
    );
}
