package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity_;
import uk.gov.hmcts.opal.util.DateTimeUtils;

public final class EnforcementSpecs {

    private EnforcementSpecs() {
    }

    public static Predicate accountHasResultPredicate(From<?, DefendantAccountEntity> account,
        CriteriaQuery<?> query, CriteriaBuilder builder, String resultId) {

        Subquery<Long> subquery = query.subquery(Long.class);
        Root<EnforcementEntity> enforcement = subquery.from(EnforcementEntity.class);

        subquery.select(builder.literal(1L));
        subquery.where(
            defendantAccountIdPredicate(enforcement, builder, DefendantAccountSpecs.defendantAccountIdPath(account)),
            resultIdPredicate(enforcement, builder, resultId));
        return builder.exists(subquery);
    }

    private static Subquery<LocalDate> postedDateForResult(From<?, DefendantAccountEntity> account,
        CriteriaQuery<?> query, CriteriaBuilder builder, String resultId, boolean earliest) {

        Subquery<LocalDate> subquery = query.subquery(LocalDate.class);
        Root<EnforcementEntity> enforcement = subquery.from(EnforcementEntity.class);
        Expression<LocalDateTime> postedDate = earliest
            ? builder.least(enforcement.get(EnforcementEntity_.postedDate))
            : builder.greatest(enforcement.get(EnforcementEntity_.postedDate));

        subquery.select(builder.function("date", LocalDate.class, postedDate));
        subquery.where(
            defendantAccountIdPredicate(enforcement, builder, DefendantAccountSpecs.defendantAccountIdPath(account)),
            resultIdPredicate(enforcement, builder, resultId));
        return subquery;
    }

    public static Subquery<LocalDate> earliestPostedDateForResultSubquery(From<?, DefendantAccountEntity> account,
        CriteriaQuery<?> query, CriteriaBuilder builder, String resultId) {

        return postedDateForResult(account, query, builder, resultId, true);
    }

    public static Subquery<LocalDate> latestPostedDateForResultSubquery(From<?, DefendantAccountEntity> account,
        CriteriaQuery<?> query, CriteriaBuilder builder, String resultId) {

        return postedDateForResult(account, query, builder, resultId, false);
    }

    public static Predicate defendantAccountIdPredicate(From<?, EnforcementEntity> from, CriteriaBuilder builder,
        Expression<Long> defendantAccountId) {

        return builder.equal(from.get(EnforcementEntity_.DEFENDANT_ACCOUNT_ID), defendantAccountId);
    }

    public static Predicate resultIdPredicate(From<?, EnforcementEntity> from, CriteriaBuilder builder,
        String resultId) {

        return builder.equal(from.get(EnforcementEntity_.RESULT_ID), resultId);
    }

    public static Predicate postedFromPredicate(From<?, EnforcementEntity> from, CriteriaBuilder builder,
        LocalDate fromDate) {

        return builder.greaterThanOrEqualTo(
            from.get(EnforcementEntity_.POSTED_DATE),
            DateTimeUtils.startOf(fromDate));
    }

    public static Predicate postedToPredicate(From<?, EnforcementEntity> from, CriteriaBuilder builder,
        LocalDate toDate) {

        return builder.lessThanOrEqualTo(
            from.get(EnforcementEntity_.POSTED_DATE),
            DateTimeUtils.endOf(toDate));
    }

    public static Predicate postedBeforePredicate(From<?, EnforcementEntity> from, CriteriaBuilder builder,
        LocalDate beforeDate) {

        return builder.lessThan(
            from.get(EnforcementEntity_.POSTED_DATE),
            DateTimeUtils.endOf(beforeDate));
    }

    public static Predicate latestActionPredicate(Root<EnforcementEntity> root, CriteriaQuery<?> query,
        CriteriaBuilder builder) {

        Subquery<LocalDateTime> maxDate = query.subquery(LocalDateTime.class);
        Root<EnforcementEntity> sub = maxDate.from(EnforcementEntity.class);

        maxDate.select(builder.greatest(sub.get(EnforcementEntity_.POSTED_DATE).as(LocalDateTime.class)));
        maxDate.where(builder.equal(
            sub.get(EnforcementEntity_.DEFENDANT_ACCOUNT),
            root.get(EnforcementEntity_.DEFENDANT_ACCOUNT)));
        return builder.equal(root.get(EnforcementEntity_.POSTED_DATE), maxDate);
    }
}
