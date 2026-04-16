package uk.gov.hmcts.opal.repository.jpa;

import static uk.gov.hmcts.opal.entity.defendantaccount.AssociationType.PARENT_GUARDIAN;
import static uk.gov.hmcts.opal.repository.jpa.EnforcementReportSpecs.enforcementSpec;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.service.report.ReportFiltersDto;

@AllArgsConstructor
public final class ReportSpecs {

    public static Specification<DefendantAccountEntity> build(ReportFiltersDto f) {
        Specification<DefendantAccountEntity> spec =
            (root, query, cb) -> cb.conjunction();

        spec = spec.and(fetchJoins());
        spec = spec.and(businessUnitSpec(f.getBusinessUnitIds()));
        spec = spec.and(accountTypesSpec(f));
        spec = spec.and(parentGuardianSpec(f));
        spec = spec.and(collectionOrderSpec(f.getCollectionOrderChoice()));
        spec = spec.and(accountStatusSpec(f.getAccountStatus()));
        spec = spec.and(balanceRangeSpec(f.getMinBalance(), f.getMaxBalance()));
        spec = spec.and(next7DaysSpec(f.getFirstPaymentOrPaybyInNext7Days()));
        spec = spec.and(nameRangeSpec(f.getLowerNameRange(), f.getUpperNameRange()));
        spec = spec.and(enforcementSpec(f));

        return spec;
    }

    private static Specification<DefendantAccountEntity> fetchJoins() {
        return (root, query, cb) -> {
            Class<?> rt = Objects.requireNonNull(query).getResultType();
            if (rt != Long.class && rt != long.class) {
                try {
                    root.fetch("parties", JoinType.LEFT).fetch("party", JoinType.LEFT);
                } catch (IllegalArgumentException ignored) {
                    ignored.getMessage();
                }
                try {
                    root.fetch("enforcingCourt", JoinType.LEFT);
                } catch (IllegalArgumentException ignored) {
                    ignored.getMessage();

                }
                try {
                    root.fetch("lastHearingCourt", JoinType.LEFT);
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
            return root.get("businessUnit").get("businessUnitId").in(buIds);
        };
    }

    private static Specification<DefendantAccountEntity> accountTypesSpec(ReportFiltersDto f) {
        return (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            Join<?, ?> link = root.join("parties", JoinType.LEFT);
            Join<?, ?> party = link.join("party", JoinType.LEFT);
            if (Boolean.TRUE.equals(f.getIncludeAdult())) {
                preds.add(cb.greaterThanOrEqualTo(party.get("age"), 18));
            }
            if (Boolean.TRUE.equals(f.getIncludeYouth())) {
                preds.add(cb.lessThan(party.get("age"), 18));
            }
            if (Boolean.TRUE.equals(f.getIncludeCompany())) {
                // company = party.organisation = true
                preds.add(cb.isTrue(party.get("organisation")));
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

            Predicate sameAccount = cb.equal(dap.get("defendantAccount").get("defendantAccountId"),
                root.get("defendantAccountId"));

            Predicate isParent = cb.equal(dap.get("associationType"), PARENT_GUARDIAN);

            sq.where(sameAccount, isParent);
            return cb.exists(sq);
        };
    }

    private static Specification<DefendantAccountEntity> collectionOrderSpec(String choice) {
        return (root, query, cb) -> {
            if (choice == null) {
                return cb.conjunction();
            }
            String c = choice.trim().toLowerCase();
            if ("with".equals(c)) {
                return cb.isTrue(root.get("collectionOrder"));
            }
            if ("without".equals(c)) {
                return cb.isFalse(root.get("collectionOrder"));
            }
            return cb.conjunction();
        };
    }

    private static Specification<DefendantAccountEntity> accountStatusSpec(String status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            String s = status.trim().toLowerCase();
            if ("live".equals(s)) {
                return cb.and(cb.greaterThan(root.get("accountBalance"), cb.literal(0)),
                    cb.isNull(root.get("completedDate")));
            }
            if ("closed".equals(s)) {
                return cb.or(cb.equal(root.get("accountBalance"), cb.literal(0)),
                    cb.isNotNull(root.get("completedDate")));
            }
            return cb.conjunction();
        };
    }

    private static Specification<DefendantAccountEntity> balanceRangeSpec(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (min != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("accountBalance"), min));
            }
            if (max != null) {
                p.add(cb.lessThanOrEqualTo(root.get("accountBalance"), max));
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
            Join<?, ?> link = root.join("parties", JoinType.LEFT);
            Join<?, ?> party = link.join("party", JoinType.LEFT);
            Expression<String> firstLetter = cb.lower(cb.substring(cb.coalesce(party.get("surname"),
                party.get("organisationName")), 1, 1));
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

            LocalDate today = LocalDate.now(ZoneId.of("Europe/London"));
            LocalDate in7 = today.plusDays(7);

            List<Predicate> preds = new ArrayList<>();

            // Try to use existing LocalDate fields on DefendantAccountEntity
            try {
                // imposedHearingDate
                Expression<LocalDate> imposed = root.get("imposedHearingDate").as(LocalDate.class);
                preds.add(cb.between(imposed, today, in7));
            } catch (IllegalArgumentException ignore) {
                // field doesn't exist — skip
            }

            try {
                // collectionOrderEffectiveDate (was collection_order_date / collectionOrderEffectiveDate)
                Expression<LocalDate> collOrder = root.get("collectionOrderEffectiveDate").as(LocalDate.class);
                preds.add(cb.between(collOrder, today, in7));
            } catch (IllegalArgumentException ignore) {
                ignore.getMessage();
            }

            try {
                // paymentCardRequestedDate
                Expression<LocalDate> payCard = root.get("paymentCardRequestedDate").as(LocalDate.class);
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