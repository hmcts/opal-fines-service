package uk.gov.hmcts.opal.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.CreditorTransactionEntity;
import uk.gov.hmcts.opal.repository.projection.MinorCreditorTransactionHistoryProjection;

@Repository
public interface CreditorTransactionRepository extends JpaRepository<CreditorTransactionEntity, Long>,
    JpaSpecificationExecutor<CreditorTransactionEntity> {

    @Query(value = """
        SELECT ct.creditor_transaction_id AS creditorTransactionId,
               ct.posted_date AS postedDate,
               ct.posted_by AS postedBy,
               ct.posted_by_name AS postedByName,
               ct.transaction_type AS transactionType,
               ct.transaction_amount AS transactionAmount,
               ct.payment_reference AS paymentReference,
               ct.status AS status,
               ct.status_date AS statusDate,
               ct.associated_record_type AS associatedRecordType,
               ct.associated_record_id AS associatedRecordId,
               ca.account_number AS accountNumber,
               COALESCE(da_direct.account_number, da_from_transaction.account_number) AS defendantAccountNumber,
               COALESCE(da_direct.defendant_account_id, da_from_transaction.defendant_account_id) AS defendantAccountId
          FROM creditor_transactions ct
          JOIN creditor_accounts ca
            ON ca.creditor_account_id = ct.creditor_account_id
          LEFT JOIN defendant_accounts da_direct
            ON ct.associated_record_type = 'defendant_accounts'::public.t_associated_record_type_enum
           AND ct.associated_record_id = da_direct.defendant_account_id::text
          LEFT JOIN defendant_transactions dt
            ON ct.associated_record_type = 'defendant_transactions'::public.t_associated_record_type_enum
           AND ct.associated_record_id = dt.defendant_transaction_id::text
          LEFT JOIN defendant_accounts da_from_transaction
            ON da_from_transaction.defendant_account_id = dt.defendant_account_id
         WHERE ct.creditor_account_id = :creditorAccountId
           AND ct.posted_date >= :postedFromInclusive
           AND ct.posted_date < :postedToExclusive
         ORDER BY ct.posted_date DESC, ct.creditor_transaction_id
        """, nativeQuery = true)
    List<MinorCreditorTransactionHistoryProjection> findMinorCreditorHistory(
        @Param("creditorAccountId") Long creditorAccountId,
        @Param("postedFromInclusive") LocalDateTime postedFromInclusive,
        @Param("postedToExclusive") LocalDateTime postedToExclusive);
}
