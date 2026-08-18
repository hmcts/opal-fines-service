package uk.gov.hmcts.opal.repository;

import java.util.function.Function;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;

@Repository
public interface InterfaceJobRepository extends JpaRepository<InterfaceJobEntity, Long>,
    JpaSpecificationExecutor<InterfaceJobEntity> {

    @Override
    @EntityGraph(attributePaths = {"businessUnit", "interfaceFiles"})
    <S extends InterfaceJobEntity, R> R findBy(
        Specification<InterfaceJobEntity> specification,
        Function<? super JpaSpecificationExecutor.SpecificationFluentQuery<S>, R> queryFunction);
}
