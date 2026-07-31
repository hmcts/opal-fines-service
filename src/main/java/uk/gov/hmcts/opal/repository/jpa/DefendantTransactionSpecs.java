package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDate;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionEntity_;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionStatus;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionType;

public class DefendantTransactionSpecs extends EntitySpecs<DefendantTransactionEntity> {

    public static Predicate accountHasPaymentFromPredicate(From<?, DefendantAccountEntity> account,
        CriteriaQuery<?> query, CriteriaBuilder builder, LocalDate earliestDate) {

        return accountHasPaymentFromPredicate(
            account, query, builder, builder.literal(earliestDate), earliestDate != null);
    }

    public static Predicate accountHasPaymentFromPredicate(From<?, DefendantAccountEntity> account,
        CriteriaQuery<?> query, CriteriaBuilder builder, Expression<LocalDate> earliestDate) {

        return accountHasPaymentFromPredicate(account, query, builder, earliestDate, true);
    }

    private static Predicate accountHasPaymentFromPredicate(From<?, DefendantAccountEntity> account,
        CriteriaQuery<?> query, CriteriaBuilder builder, Expression<LocalDate> earliestDate,
        boolean includeEarliestDate) {

        Subquery<Long> subquery = query.subquery(Long.class);
        Root<DefendantTransactionEntity> transaction = subquery.from(DefendantTransactionEntity.class);

        subquery.select(builder.literal(1L));
        subquery.where(
            equalsDefendantAccountIdPredicate(
                transaction, builder, DefendantAccountSpecs.defendantAccountIdPath(account)),
            paymentFromPredicate(transaction, builder, earliestDate, includeEarliestDate));
        return builder.exists(subquery);
    }

    public static Predicate accountHasPaymentFromEnforcementPredicate(
        From<?, DefendantAccountEntity> account, CriteriaQuery<?> query, CriteriaBuilder builder, String resultId,
        boolean firstEnforcement) {

        Expression<LocalDate> enforcementDate = firstEnforcement
            ? EnforcementSpecs.earliestPostedDateForResultSubquery(account, query, builder, resultId)
            : EnforcementSpecs.latestPostedDateForResultSubquery(account, query, builder, resultId);
        return accountHasPaymentFromPredicate(account, query, builder, enforcementDate);
    }

    public static Predicate equalsDefendantAccountIdPredicate(From<?, DefendantTransactionEntity> from,
        CriteriaBuilder builder, Long defendantAccountId) {
        return builder.equal(from.get(DefendantTransactionEntity_.defendantAccountId), defendantAccountId);
    }

    public static Predicate equalsDefendantAccountIdPredicate(From<?, DefendantTransactionEntity> from,
        CriteriaBuilder builder, Expression<Long> defendantAccountId) {
        return builder.equal(from.get(DefendantTransactionEntity_.defendantAccountId), defendantAccountId);
    }

    public static Predicate paymentFromPredicate(From<?, DefendantTransactionEntity> from, CriteriaBuilder cb,
        LocalDate earliestDate) {
        return paymentFromPredicate(from, cb, cb.literal(earliestDate), earliestDate != null);
    }

    public static Predicate paymentFromPredicate(From<?, DefendantTransactionEntity> from, CriteriaBuilder cb,
        Expression<LocalDate> earliestDate) {
        return paymentFromPredicate(from, cb, earliestDate, true);
    }

    private static Predicate paymentFromPredicate(From<?, DefendantTransactionEntity> from, CriteriaBuilder cb,
        Expression<LocalDate> earliestDate, boolean includeEarliestDate) {
        Predicate isPayment = cb.and(
            cb.equal(from.get(DefendantTransactionEntity_.ASSOCIATED_RECORD_TYPE),
                AssociatedRecordType.DEFENDANT_ACCOUNTS),
            from.get(DefendantTransactionEntity_.TRANSACTION_TYPE)
                .in(DefendantTransactionType.PAYMNT, DefendantTransactionType.CHEQUE),
            from.get(DefendantTransactionEntity_.STATUS)
                .in(DefendantTransactionStatus.C, DefendantTransactionStatus.P));
        if (includeEarliestDate) {
            return cb.and(
                isPayment,
                cb.greaterThanOrEqualTo(from.get(DefendantTransactionEntity_.POSTED_DATE), earliestDate));
        }
        return isPayment;
    }
}
