package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionEntity_;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionStatus;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionType;

public class DefendantTransactionSpecs extends EntitySpecs<DefendantTransactionEntity> {

    public static Predicate equalsDefendantAccountIdPredicate(From<?, DefendantTransactionEntity> from,
        CriteriaBuilder builder, Long defendantAccountId) {
        return builder.equal(from.get(DefendantTransactionEntity_.defendantAccountId), defendantAccountId);
    }

    public static Predicate paymentMadeOnOrAfterDatePredicate(
        Root<DefendantTransactionEntity> root,
        CriteriaBuilder cb,
        LocalDate earliestDate
    ) {
        Predicate isPayment = cb.and(
            cb.equal(root.get(DefendantTransactionEntity_.ASSOCIATED_RECORD_TYPE),
                AssociatedRecordType.DEFENDANT_ACCOUNTS),
            root.get(DefendantTransactionEntity_.TRANSACTION_TYPE)
                .in(DefendantTransactionType.PAYMNT, DefendantTransactionType.CHEQUE),
            root.get(DefendantTransactionEntity_.STATUS)
                .in(DefendantTransactionStatus.CLEARED_PRESENTED, DefendantTransactionStatus.PARTIALLY_REVERSED)
        );
        if (earliestDate != null) {
            return cb.and(
                isPayment,
                cb.greaterThanOrEqualTo(root.get(DefendantTransactionEntity_.POSTED_DATE), earliestDate)
            );
        }
        return isPayment;
    }
}
