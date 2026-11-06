package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.result.ResultLegacyEntity;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Repository
public interface ResultRepository extends JpaRepository<ResultLegacyEntity, String>,
    JpaSpecificationExecutor<ResultLegacyEntity> {

    @Override
    @EntityGraph(value = ResultLegacyEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    Optional<ResultLegacyEntity> findById(String resultId);

    @EntityGraph(value = ResultLegacyEntity.ENTITY_GRAPH_FULL, type = EntityGraph.EntityGraphType.FETCH)
    Optional<ResultLegacyEntity> findWithFullGraphByResultId(String resultId);

    @Override
    @EntityGraph(value = ResultLegacyEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    <S extends ResultLegacyEntity, R> R findBy(
        Specification<ResultLegacyEntity> spec,
        Function<? super JpaSpecificationExecutor.SpecificationFluentQuery<S>, R> queryFunction
    );

    @EntityGraph(value = ResultLegacyEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    List<ResultLegacyEntity> findByResultIdIn(List<String> resultIds);
}
