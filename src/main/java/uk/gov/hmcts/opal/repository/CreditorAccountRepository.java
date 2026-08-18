package uk.gov.hmcts.opal.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select c from CreditorAccountEntity c where c.creditorAccountId = :id")
    Optional<CreditorAccountEntity> findByCreditorAccountIdForUpdate(@Param("id") Long creditorAccountId);

    @Query(value = """
        SELECT ca.creditor_account_id AS "creditorAccountId",
               ca.account_number AS "accountNumber",
               ci.item_values ->> 'name' AS "name",
               bu.business_unit_id AS "businessUnitId",
               bu.business_unit_name AS "businessUnitName",
               bu.welsh_language AS "welshLanguage",
               ca.version_number AS "versionNumber"
          FROM creditor_accounts ca
          JOIN business_units bu
            ON bu.business_unit_id = ca.business_unit_id
          JOIN configuration_items ci
            ON ci.business_unit_id = bu.business_unit_id
           AND ci.item_name = 'CENTRAL_FUND_ACCOUNT'
         WHERE ca.business_unit_id = :businessUnitId
           AND ca.creditor_account_type = 'CF'::public.t_creditor_account_type_enum
         ORDER BY ca.creditor_account_id
         LIMIT 1
        """, nativeQuery = true)
    Optional<CentralFundProjection> findCentralFundByBusinessUnitId(@Param("businessUnitId") Short businessUnitId);
}
