package uk.gov.hmcts.opal.repository.jpa;

import static uk.gov.hmcts.opal.dto.AccountStatusReportFilterType.CLOSED;
import static uk.gov.hmcts.opal.dto.AccountStatusReportFilterType.LIVE;
import static uk.gov.hmcts.opal.dto.CollectionOrderReportFilterType.WITH;
import static uk.gov.hmcts.opal.dto.CollectionOrderReportFilterType.WITHOUT;
import static uk.gov.hmcts.opal.entity.defendantaccount.AssociationType.PARENT_GUARDIAN;
import static uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity_.ACCOUNT_BALANCE;
import static uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity_.BUSINESS_UNIT;
import static uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity_.COLLECTION_ORDER;
import static uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity_.COLLECTION_ORDER_EFFECTIVE_DATE;
import static uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity_.COMPLETED_DATE;
import static uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity_.DEFENDANT_ACCOUNT_ID;
import static uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity_.ENFORCING_COURT;
import static uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity_.IMPOSED_HEARING_DATE;
import static uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity_.LAST_HEARING_COURT;
import static uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity_.PARTIES;
import static uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity_.PAYMENT_CARD_REQUESTED_DATE;
import static uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity_.ASSOCIATION_TYPE;
import static uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity_.DEFENDANT_ACCOUNT;
import static uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity_.PARTY;
import static uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountSummaryViewEntity_.AGE;
import static uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountSummaryViewEntity_.ORGANISATION;
import static uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountSummaryViewEntity_.ORGANISATION_NAME;
import static uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountSummaryViewEntity_.SURNAME;
import static uk.gov.hmcts.opal.entity.search.SearchDefendantAccount_.BUSINESS_UNIT_ID;
import static uk.gov.hmcts.opal.util.AgeUtil.ADULT_AGE;
import static uk.gov.hmcts.opal.util.DateTimeUtils.todayPlusDaysUk;
import static uk.gov.hmcts.opal.util.DateTimeUtils.todayUk;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.AccountStatusReportFilterType;
import uk.gov.hmcts.opal.dto.CollectionOrderReportFilterType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.service.report.ReportFiltersDto;

@AllArgsConstructor
public final class ReportSpecs {

    public static Specification<DefendantAccountEntity> build(ReportFiltersDto filters) {
        Specification<DefendantAccountEntity> spec =
            (root, query, cb) -> cb.conjunction();
        spec = spec.and(fetchJoins())
            .and(businessUnitSpec(filters.getBusinessUnitIds()))
            .and(accountTypesSpec(filters))
            .and(parentGuardianSpec(filters))
            .and(collectionOrderSpec(filters.getCollectionOrderChoice()))
            .and(accountStatusSpec(filters.getAccountStatus()))
            .and(balanceRangeSpec(filters.getMinBalance(), filters.getMaxBalance()))
            .and(next7DaysSpec(filters.getFirstPaymentOrPayByInNext7Days()))
            .and(nameRangeSpec(filters.getLowerNameRange(), filters.getUpperNameRange()))
            .and(EnforcementReportSpecs.enforcementSpec(filters));

        return spec;
    }

    private static Specification<DefendantAccountEntity> fetchJoins() {
        return (root, query, cb) -> {
            Class<?> rt = Objects.requireNonNull(query).getResultType();
            if (rt != Long.class && rt != long.class) {
                try {
                    root.fetch(PARTIES, JoinType.LEFT).fetch(PARTY, JoinType.LEFT);
                } catch (IllegalArgumentException ignored) {
                    ignored.getMessage();
                }
                try {
                    root.fetch(ENFORCING_COURT, JoinType.LEFT);
                } catch (IllegalArgumentException ignored) {
                    ignored.getMessage();

                }
                try {
                    root.fetch(LAST_HEARING_COURT, JoinType.LEFT);
                } catch (IllegalArgumentException ignored) {
                    ignored.getMessage();
                }
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }

    private static Specification<DefendantAccountEntity> businessUnitSpec(List<Long> buIds) {
        return (root, query, cb) -> {
            if (buIds == null || buIds.isEmpty()) {
                return cb.conjunction();
            }
            return root.get(BUSINESS_UNIT).get(BUSINESS_UNIT_ID).in(buIds);
        };
    }

    private static Specification<DefendantAccountEntity> accountTypesSpec(ReportFiltersDto f) {
        return (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            Join<?, ?> link = root.join(PARTIES, JoinType.LEFT);
            Join<?, ?> party = link.join(PARTY, JoinType.LEFT);
            if (Boolean.TRUE.equals(f.getIncludeAdult())) {
                preds.add(cb.greaterThanOrEqualTo(party.get(AGE), ADULT_AGE));
            }
            if (Boolean.TRUE.equals(f.getIncludeYouth())) {
                preds.add(cb.lessThan(party.get(AGE), ADULT_AGE));
            }
            if (Boolean.TRUE.equals(f.getIncludeCompany())) {
                // company = party.organisation = true
                preds.add(cb.isTrue(party.get(ORGANISATION)));
            }
            if (preds.isEmpty()) {
                return cb.conjunction();
            }
            return cb.or(preds.toArray(new Predicate[0]));
        };
    }

    /**
     * Filter for accounts that have a defendant_account_parties association_type indicating a parent/guardian. Uses the
     * exact association_type value "Parent/Guardian" (case-insensitive).
     */
    private static Specification<DefendantAccountEntity> parentGuardianSpec(ReportFiltersDto f) {
        return (root, query, cb) -> {
            if (!Boolean.TRUE.equals(f.getOnlyAccountsWithParentGuardian())) {
                return cb.conjunction();
            }

            Subquery<Long> sq = Objects.requireNonNull(query).subquery(Long.class);
            Root<?> dap = sq.from(DefendantAccountPartiesEntity.class);
            sq.select(cb.literal(1L));

            Predicate sameAccount = cb.equal(dap.get(DEFENDANT_ACCOUNT).get(DEFENDANT_ACCOUNT_ID),
                root.get(DEFENDANT_ACCOUNT_ID));

            Predicate isParent = cb.equal(dap.get(ASSOCIATION_TYPE), PARENT_GUARDIAN);

            sq.where(sameAccount, isParent);
            return cb.exists(sq);
        };
    }

    private static Specification<DefendantAccountEntity> collectionOrderSpec(CollectionOrderReportFilterType choice) {
        return (root, query, cb) -> {
            if (choice == null) {
                return cb.conjunction();
            }
            if (choice.equals(WITH)) {
                return cb.isTrue(root.get(COLLECTION_ORDER));
            }
            if (choice.equals(WITHOUT)) {
                return cb.isFalse(root.get(COLLECTION_ORDER));
            }
            return cb.conjunction();
        };
    }

    private static Specification<DefendantAccountEntity> accountStatusSpec(AccountStatusReportFilterType status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            if (status.equals(LIVE)) {
                return cb.and(cb.greaterThan(root.get(ACCOUNT_BALANCE), cb.literal(0)),
                    cb.isNull(root.get(COMPLETED_DATE)));
            }
            if (status.equals(CLOSED)) {
                return cb.or(cb.equal(root.get(ACCOUNT_BALANCE), cb.literal(0)),
                    cb.isNotNull(root.get(ACCOUNT_BALANCE)));
            }
            return cb.conjunction();
        };
    }

    private static Specification<DefendantAccountEntity> balanceRangeSpec(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (min != null) {
                p.add(cb.greaterThanOrEqualTo(root.get(ACCOUNT_BALANCE), min));
            }
            if (max != null) {
                p.add(cb.lessThanOrEqualTo(root.get(ACCOUNT_BALANCE), max));
            }
            if (p.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    private static Specification<DefendantAccountEntity> nameRangeSpec(String lower, String upper) {
        return (root, query, cb) -> {
            if (lower == null && upper == null) {
                return cb.conjunction();
            }
            Join<?, ?> link = root.join(PARTIES, JoinType.LEFT);
            Join<?, ?> party = link.join(PARTY, JoinType.LEFT);
            Expression<String> firstLetter = cb.lower(cb.substring(cb.coalesce(party.get(SURNAME),
                party.get(ORGANISATION_NAME)), 1, 1));
            List<Predicate> p = new ArrayList<>();
            if (lower != null) {
                p.add(cb.greaterThanOrEqualTo(firstLetter, lower.toLowerCase()));
            }
            if (upper != null) {
                p.add(cb.lessThanOrEqualTo(firstLetter, upper.toLowerCase()));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    private static Specification<DefendantAccountEntity> next7DaysSpec(Boolean next7) {
        return (root, query, cb) -> {
            if (!Boolean.TRUE.equals(next7)) {
                return cb.conjunction();
            }
            LocalDate today = todayUk();
            LocalDate in7 = todayPlusDaysUk(7);
            List<Predicate> preds = new ArrayList<>();
            try {
                Expression<LocalDate> imposed = root.get(IMPOSED_HEARING_DATE).as(LocalDate.class);
                preds.add(cb.between(imposed, today, in7));
            } catch (IllegalArgumentException ignore) {
                // field doesn't exist — skip
            }

            try {
                Expression<LocalDate> collOrder = root.get(COLLECTION_ORDER_EFFECTIVE_DATE).as(LocalDate.class);
                preds.add(cb.between(collOrder, today, in7));
            } catch (IllegalArgumentException ignore) {
                ignore.getMessage();
            }

            try {
                Expression<LocalDate> payCard = root.get(PAYMENT_CARD_REQUESTED_DATE).as(LocalDate.class);
                preds.add(cb.between(payCard, today, in7));
            } catch (IllegalArgumentException ignore) {
                ignore.getMessage();
            }

            if (preds.isEmpty()) {
                return cb.conjunction();
            }
            return cb.or(preds.toArray(new Predicate[0]));
        };
    }
}