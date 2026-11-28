package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.result.ResultOpalEntity;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Repository
public interface ResultOpalRepository extends JpaRepository<ResultOpalEntity, String>,
    JpaSpecificationExecutor<ResultOpalEntity> {

    @Override
    @EntityGraph(value = ResultOpalEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    Optional<ResultOpalEntity> findById(String resultId);

    @EntityGraph(value = ResultOpalEntity.ENTITY_GRAPH_FULL, type = EntityGraph.EntityGraphType.FETCH)
    Optional<ResultOpalEntity> findWithFullGraphByResultId(String resultId);

    @Override
    @EntityGraph(value = ResultOpalEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    <S extends ResultOpalEntity, R> R findBy(
        Specification<ResultOpalEntity> spec,
        Function<? super JpaSpecificationExecutor.SpecificationFluentQuery<S>, R> queryFunction
    );

    @EntityGraph(value = ResultOpalEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    List<ResultOpalEntity> findByResultIdIn(List<String> resultIds);
}
