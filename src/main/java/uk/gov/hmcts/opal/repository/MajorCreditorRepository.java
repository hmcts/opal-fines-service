package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorEntity;

import java.util.Optional;
import java.util.function.Function;

@Repository
public interface MajorCreditorRepository extends JpaRepository<MajorCreditorEntity, Long>,
    JpaSpecificationExecutor<MajorCreditorEntity> {

    @Override
    @EntityGraph(value = MajorCreditorEntity.ENTITY_GRAPH_FULL, type = EntityGraph.EntityGraphType.FETCH)
    Optional<MajorCreditorEntity> findById(Long majorCreditorId);

    @Override
    @EntityGraph(value = MajorCreditorEntity.ENTITY_GRAPH_FULL, type = EntityGraph.EntityGraphType.FETCH)
    <S extends MajorCreditorEntity, R> R findBy(
        Specification<MajorCreditorEntity> spec,
        Function<? super JpaSpecificationExecutor.SpecificationFluentQuery<S>, R> queryFunction
    );
}
