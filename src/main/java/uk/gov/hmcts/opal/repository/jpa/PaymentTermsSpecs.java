package uk.gov.hmcts.opal.repository.jpa;

import static uk.gov.hmcts.opal.util.DateTimeUtils.todayPlusDaysUk;
import static uk.gov.hmcts.opal.util.DateTimeUtils.todayUk;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDate;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity_;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity_;

public final class PaymentTermsSpecs {

    private PaymentTermsSpecs() {
    }

    public static Predicate effectiveInNextDaysPredicate(
        From<?, DefendantAccountEntity> account, CriteriaQuery<?> query, CriteriaBuilder builder, int days) {

        LocalDate today = todayUk();
        LocalDate effectiveDateTo = todayPlusDaysUk(days);
        return effectiveBetweenPredicate(account, query, builder, today, effectiveDateTo);
    }

    public static Predicate effectiveBetweenPredicate(
        From<?, DefendantAccountEntity> account, CriteriaQuery<?> query, CriteriaBuilder builder,
        LocalDate effectiveDateFrom, LocalDate effectiveDateTo) {

        Subquery<Long> subquery = query.subquery(Long.class);
        Root<PaymentTermsEntity> paymentTerms = subquery.from(PaymentTermsEntity.class);

        subquery.select(builder.literal(1L));
        subquery.where(
            defendantAccountIdPredicate(paymentTerms, builder, DefendantAccountSpecs.defendantAccountIdPath(account)),
            effectiveDateBetweenPredicate(paymentTerms, builder, effectiveDateFrom, effectiveDateTo));
        return builder.exists(subquery);
    }

    public static Predicate defendantAccountIdPredicate(From<?, PaymentTermsEntity> from,
        CriteriaBuilder builder, Expression<Long> defendantAccountId) {

        return builder.equal(
            from.get(PaymentTermsEntity_.DEFENDANT_ACCOUNT).get(DefendantAccountEntity_.DEFENDANT_ACCOUNT_ID),
            defendantAccountId);
    }

    public static Predicate effectiveDateBetweenPredicate(From<?, PaymentTermsEntity> from, CriteriaBuilder builder,
        LocalDate effectiveDateFrom, LocalDate effectiveDateTo) {

        return builder.between(from.get(PaymentTermsEntity_.EFFECTIVE_DATE), effectiveDateFrom, effectiveDateTo);
    }
}
