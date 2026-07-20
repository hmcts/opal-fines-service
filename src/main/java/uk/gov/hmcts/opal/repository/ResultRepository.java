package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.result.ResultEntity;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Repository
public interface ResultRepository extends JpaRepository<ResultEntity, String>,
    JpaSpecificationExecutor<ResultEntity> {

    @Override
    @EntityGraph(value = ResultEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    Optional<ResultEntity> findById(String resultId);

    @EntityGraph(value = ResultEntity.ENTITY_GRAPH_FULL, type = EntityGraph.EntityGraphType.FETCH)
    Optional<ResultEntity> findWithFullGraphByResultId(String resultId);

    @Override
    @EntityGraph(value = ResultEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    <S extends ResultEntity, R> R findBy(
        Specification<ResultEntity> spec,
        Function<? super JpaSpecificationExecutor.SpecificationFluentQuery<S>, R> queryFunction
    );

    @EntityGraph(value = ResultEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    List<ResultEntity> findByResultIdIn(List<String> resultIds);
}
