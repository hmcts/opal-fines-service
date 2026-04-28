package uk.gov.hmcts.opal.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;

@Repository
public interface CreditorAccountRepository extends JpaRepository<CreditorAccountEntity, Long>,
    JpaSpecificationExecutor<CreditorAccountEntity> {

    @Override
    @EntityGraph(value = CreditorAccountEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    Optional<CreditorAccountEntity> findById(Long creditorAccountId);

    @EntityGraph(value = CreditorAccountEntity.ENTITY_GRAPH_FULL, type = EntityGraph.EntityGraphType.FETCH)
    Optional<CreditorAccountEntity> findFullByCreditorAccountId(Long creditorAccountId);

    @EntityGraph(value = CreditorAccountEntity.ENTITY_GRAPH_LITE, type = EntityGraph.EntityGraphType.FETCH)
    Optional<CreditorAccountEntity> findByCreditorAccountIdAndBusinessUnitId(
        Long creditorAccountId,
        Short businessUnitId
    );
}
