package uk.gov.hmcts.opal.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.projection.DefendantAccountVersionData;

@Repository
public interface DefendantAccountRepository extends JpaRepository<DefendantAccountEntity, Long>,
        JpaSpecificationExecutor<DefendantAccountEntity> {

    List<DefendantAccountEntity> findAllByBusinessUnit_BusinessUnitId(Short businessUnitId);

    Optional<DefendantAccountEntity> findByDefendantAccountId(Long defendantAccountId);

    List<DefendantAccountEntity> findAllByDefendantAccountIdIn(List<Long> defendantAccountIds);

    @Query("""
        SELECT new uk.gov.hmcts.opal.entity.projection.DefendantAccountVersionData(
            defendantAccount.defendantAccountId,
            defendantAccount.versionNumber)
        FROM DefendantAccountEntity defendantAccount
        WHERE defendantAccount.defendantAccountId = :defendantAccountId
        """)
    Optional<DefendantAccountVersionData> findVersionDataByDefendantAccountId(
        @Param("defendantAccountId") Long defendantAccountId);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select d from DefendantAccountEntity d where d.defendantAccountId = :id")
    Optional<DefendantAccountEntity> findByDefendantAccountIdForUpdate(@Param("id") Long defendantAccountId);

    @Query(value = """
        select da.*
        from defendant_accounts da
        join (
            select e.defendant_account_id, min(e.posted_date) as cutoff_date
            from enforcements e
            where e.result_id = 'REGF'
            group by e.defendant_account_id
        ) cutoff on cutoff.defendant_account_id = da.defendant_account_id
        where (:paymentMade = exists (
            select 1
            from defendant_transactions dt
            where dt.defendant_account_id = da.defendant_account_id
              and dt.posted_date >= cutoff.cutoff_date::date
              and dt.transaction_type::text in ('PAYMNT', 'CHEQUE')
              and dt.status::text in ('C', 'P')
              and dt.associated_record_type::text = 'defendant_accounts'
        ))
        order by da.account_number
        """, nativeQuery = true)
    List<DefendantAccountEntity> findAccountsWithPaymentMadeAfterFirstRegfEnforcement(
        @Param("paymentMade") boolean paymentMade
    );

    @Query(value = """
        select da.*
        from defendant_accounts da
        join (
            select e.defendant_account_id, max(e.posted_date) as cutoff_date
            from enforcements e
            where e.result_id = :enforcementResultId
            group by e.defendant_account_id
        ) cutoff on cutoff.defendant_account_id = da.defendant_account_id
        where (:paymentMade = exists (
            select 1
            from defendant_transactions dt
            where dt.defendant_account_id = da.defendant_account_id
              and dt.posted_date >= cutoff.cutoff_date::date
              and dt.transaction_type::text in ('PAYMNT', 'CHEQUE')
              and dt.status::text in ('C', 'P')
              and dt.associated_record_type::text = 'defendant_accounts'
        ))
        order by da.account_number
        """, nativeQuery = true)
    List<DefendantAccountEntity> findAccountsWithPaymentMadeAfterLastEnforcementAction(
        @Param("enforcementResultId") String enforcementResultId,
        @Param("paymentMade") boolean paymentMade
    );
}
