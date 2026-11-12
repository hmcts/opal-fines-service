package uk.gov.hmcts.opal.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.SuspenseTransactionEntity;
import uk.gov.hmcts.opal.entity.projection.SuspenseTransactionHistoryProjection;

@Repository
public interface SuspenseTransactionRepository extends JpaRepository<SuspenseTransactionEntity, Long> {

    @Query(value = """
        SELECT st.suspense_transaction_id,
               st.transaction_type::text AS transaction_type,
               st.amount,
               st.posted_date,
               st.posted_by,
               st.posted_by_name,
               st.associated_record_type::text AS associated_record_type,
               st.associated_record_id,
               st.text,
               st.reversed::text AS reversed
        FROM public.suspense_transactions st
        JOIN public.suspense_items si
          ON si.suspense_item_id = st.suspense_item_id
        JOIN public.suspense_accounts sa
          ON sa.suspense_account_id = si.suspense_account_id
        WHERE si.suspense_item_id = :suspenseItemId
          AND sa.business_unit_id = :businessUnitId
        ORDER BY st.posted_date DESC, st.suspense_transaction_id DESC;
        """, nativeQuery = true)
    List<SuspenseTransactionHistoryProjection> findHistory(
        @Param("suspenseItemId") Long suspenseItemId,
        @Param("businessUnitId") Long businessUnitId);
}
