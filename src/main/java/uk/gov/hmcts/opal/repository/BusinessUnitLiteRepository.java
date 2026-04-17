package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;

import java.util.function.Function;

@Repository
public interface BusinessUnitLiteRepository extends JpaRepository<BusinessUnitEntity, Short>,
    JpaSpecificationExecutor<BusinessUnitEntity> {

    @Override
    @EntityGraph(value = BusinessUnitEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    <S extends BusinessUnitEntity, R> R findBy(
        Specification<BusinessUnitEntity> spec,
        Function<? super JpaSpecificationExecutor.SpecificationFluentQuery<S>, R> queryFunction
    );
}
