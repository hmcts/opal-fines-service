package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.ReportEntryEntity;

@Repository
public interface ReportEntryRepository extends JpaRepository<ReportEntryEntity, Long> {

    @Modifying
    @Query("""
        DELETE FROM ReportEntryEntity r
        WHERE r.associatedRecordType = 'payment_terms'
        AND r.associatedRecordId IN (
            SELECT CAST(p.paymentTermsId AS string)
            FROM PaymentTermsEntity p
            WHERE p.defendantAccount.defendantAccountId = :defendantAccountId
        )
        """)
    void deletePaymentTermsReportEntriesByDefendantAccountId(
        @Param("defendantAccountId") long defendantAccountId
    );
}
