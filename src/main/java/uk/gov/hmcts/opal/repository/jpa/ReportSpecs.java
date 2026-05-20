package uk.gov.hmcts.opal.repository.jpa;

import static uk.gov.hmcts.opal.dto.AccountStatusReportFilterType.CLOSED;
import static uk.gov.hmcts.opal.dto.AccountStatusReportFilterType.LIVE;
import static uk.gov.hmcts.opal.dto.CollectionOrderReportFilterType.WITH;
import static uk.gov.hmcts.opal.dto.CollectionOrderReportFilterType.WITHOUT;
import static uk.gov.hmcts.opal.entity.defendantaccount.AssociationType.PARENT_GUARDIAN;
import static uk.gov.hmcts.opal.util.AgeUtil.ADULT_AGE;
import static uk.gov.hmcts.opal.util.DateTimeUtils.todayPlusDaysUk;
import static uk.gov.hmcts.opal.util.DateTimeUtils.todayUk;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.entity.PartyEntity_;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity_;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity_;
import uk.gov.hmcts.opal.entity.search.SearchDefendantAccount_;
import uk.gov.hmcts.opal.service.report.ReportFiltersDto;

@AllArgsConstructor
public final class ReportSpecs {

    public static Specification<DefendantAccountEntity> build(ReportFiltersDto filters) {
        return Specification.where(fetchJoins())
            .and(accountFiltersSpec(filters));
    }

    public static Specification<DefendantAccountEntity> accountFiltersSpec(ReportFiltersDto filters) {
        return (root, query, cb) ->
            accountFilters(root, query, cb, filters);
    }

    public static Predicate accountFilters(
        From<?, DefendantAccountEntity> root,
        CriteriaQuery<?> query,
        CriteriaBuilder cb,
        ReportFiltersDto filters
    ) {
        List<Predicate> preds = new ArrayList<>();

        addBusinessUnitFilter(root, filters, preds);
        addAccountTypeFilter(root, cb, filters, preds);
        addParentGuardianFilter(root, query, cb, filters, preds);
        addCollectionOrderFilter(root, cb, filters, preds);
        addAccountStatusFilter(root, cb, filters, preds);
        addBalanceFilter(root, cb, filters, preds);
        addNameRangeFilter(root, cb, filters, preds);
        addNext7DaysFilter(root, cb, filters, preds);

        return preds.isEmpty()
            ? cb.conjunction()
            : cb.and(preds.toArray(new Predicate[0]));
    }

    private static void addBusinessUnitFilter(
        From<?, DefendantAccountEntity> root,
        ReportFiltersDto filters,
        List<Predicate> preds
    ) {
        if (filters.getBusinessUnitIds() != null && !filters.getBusinessUnitIds().isEmpty()) {
            preds.add(root.get(DefendantAccountEntity_.BUSINESS_UNIT)
                .get(SearchDefendantAccount_.BUSINESS_UNIT_ID)
                .in(filters.getBusinessUnitIds()));
        }
    }

    private static void addAccountTypeFilter(
        From<?, DefendantAccountEntity> root,
        CriteriaBuilder cb,
        ReportFiltersDto filters,
        List<Predicate> preds
    ) {
        if (!(Boolean.TRUE.equals(filters.getIncludeAdult())
            || Boolean.TRUE.equals(filters.getIncludeYouth())
            || Boolean.TRUE.equals(filters.getIncludeCompany()))) {
            return;
        }

        Join<?, ?> link = root.join(DefendantAccountEntity_.PARTIES, JoinType.LEFT);
        Join<?, ?> party = link.join(DefendantAccountPartiesEntity_.PARTY, JoinType.LEFT);

        List<Predicate> typePreds = new ArrayList<>();

        if (Boolean.TRUE.equals(filters.getIncludeAdult())) {
            typePreds.add(cb.greaterThanOrEqualTo(party.get(PartyEntity_.AGE), ADULT_AGE));
        }
        if (Boolean.TRUE.equals(filters.getIncludeYouth())) {
            typePreds.add(cb.lessThan(party.get(PartyEntity_.AGE), ADULT_AGE));
        }
        if (Boolean.TRUE.equals(filters.getIncludeCompany())) {
            typePreds.add(cb.isTrue(party.get(PartyEntity_.ORGANISATION)));
        }

        preds.add(cb.or(typePreds.toArray(new Predicate[0])));
    }

    private static void addParentGuardianFilter(
        From<?, DefendantAccountEntity> root,
        CriteriaQuery<?> query,
        CriteriaBuilder cb,
        ReportFiltersDto filters,
        List<Predicate> preds
    ) {
        if (!Boolean.TRUE.equals(filters.getOnlyAccountsWithParentGuardian())) {
            return;
        }

        Subquery<Long> sq = query.subquery(Long.class);
        Root<DefendantAccountPartiesEntity> dap = sq.from(DefendantAccountPartiesEntity.class);

        sq.select(cb.literal(1L));
        sq.where(
            cb.equal(
                dap.get(DefendantAccountPartiesEntity_.DEFENDANT_ACCOUNT).get(
                    DefendantAccountEntity_.DEFENDANT_ACCOUNT_ID),
                root.get(DefendantAccountEntity_.DEFENDANT_ACCOUNT_ID)
            ),
            cb.equal(dap.get(DefendantAccountPartiesEntity_.ASSOCIATION_TYPE), PARENT_GUARDIAN)
        );

        preds.add(cb.exists(sq));
    }

    private static void addCollectionOrderFilter(
        From<?, DefendantAccountEntity> root,
        CriteriaBuilder cb,
        ReportFiltersDto filters,
        List<Predicate> preds
    ) {
        if (filters.getCollectionOrderChoice() == null) {
            return;
        }

        if (filters.getCollectionOrderChoice().equals(WITH)) {
            preds.add(cb.isTrue(root.get(DefendantAccountEntity_.COLLECTION_ORDER)));
        } else if (filters.getCollectionOrderChoice().equals(WITHOUT)) {
            preds.add(cb.isFalse(root.get(DefendantAccountEntity_.COLLECTION_ORDER)));
        }
    }

    private static void addAccountStatusFilter(
        From<?, DefendantAccountEntity> root,
        CriteriaBuilder cb,
        ReportFiltersDto filters,
        List<Predicate> preds
    ) {
        if (filters.getAccountStatus() == null) {
            return;
        }

        if (filters.getAccountStatus().equals(LIVE)) {
            preds.add(cb.and(
                cb.greaterThan(root.get(DefendantAccountEntity_.ACCOUNT_BALANCE), cb.literal(0)),
                cb.isNull(root.get(DefendantAccountEntity_.COMPLETED_DATE))
            ));
        } else if (filters.getAccountStatus().equals(CLOSED)) {
            preds.add(cb.or(
                cb.equal(root.get(DefendantAccountEntity_.ACCOUNT_BALANCE), cb.literal(0)),
                cb.isNotNull(root.get(DefendantAccountEntity_.COMPLETED_DATE))
            ));
        }
    }

    private static void addBalanceFilter(
        From<?, DefendantAccountEntity> root,
        CriteriaBuilder cb,
        ReportFiltersDto filters,
        List<Predicate> preds
    ) {
        if (filters.getMinBalance() != null) {
            preds.add(
                cb.greaterThanOrEqualTo(root.get(DefendantAccountEntity_.ACCOUNT_BALANCE), filters.getMinBalance()));
        }
        if (filters.getMaxBalance() != null) {
            preds.add(cb.lessThanOrEqualTo(root.get(DefendantAccountEntity_.ACCOUNT_BALANCE), filters.getMaxBalance()));
        }
    }

    private static void addNameRangeFilter(
        From<?, DefendantAccountEntity> root,
        CriteriaBuilder cb,
        ReportFiltersDto filters,
        List<Predicate> preds
    ) {
        if (filters.getLowerNameRange() == null && filters.getUpperNameRange() == null) {
            return;
        }

        Join<?, ?> link = root.join(DefendantAccountEntity_.PARTIES, JoinType.LEFT);
        Join<?, ?> party = link.join(DefendantAccountPartiesEntity_.PARTY, JoinType.LEFT);

        Expression<String> firstLetter = cb.lower(
            cb.substring(
                cb.coalesce(party.get(PartyEntity_.SURNAME), party.get(PartyEntity_.ORGANISATION_NAME)),
                1, 1
            )
        );

        if (filters.getLowerNameRange() != null) {
            preds.add(cb.greaterThanOrEqualTo(firstLetter, filters.getLowerNameRange().toLowerCase()));
        }
        if (filters.getUpperNameRange() != null) {
            preds.add(cb.lessThanOrEqualTo(firstLetter, filters.getUpperNameRange().toLowerCase()));
        }
    }


    private static void addNext7DaysFilter(
        From<?, DefendantAccountEntity> root,
        CriteriaBuilder cb,
        ReportFiltersDto filters,
        List<Predicate> preds
    ) {
        if (!Boolean.TRUE.equals(filters.getFirstPaymentOrPayByInNext7Days())) {
            return;
        }
        LocalDate today = todayUk();
        LocalDate in7Days = todayPlusDaysUk(7);
        List<Predicate> datePreds = new ArrayList<>();

        List<String> fields = List.of(
            DefendantAccountEntity_.IMPOSED_HEARING_DATE,
            DefendantAccountEntity_.COLLECTION_ORDER_EFFECTIVE_DATE,
            DefendantAccountEntity_.PAYMENT_CARD_REQUESTED_DATE
        );
        fields.forEach(f -> addBetweenTwoDatesConditionIfFieldPresent(root, cb, datePreds, f, today, in7Days));
        if (!datePreds.isEmpty()) {
            preds.add(cb.or(datePreds.toArray(new Predicate[0])));
        }
    }

    private static void addBetweenTwoDatesConditionIfFieldPresent(
        From<?, ?> root,
        CriteriaBuilder cb,
        List<Predicate> preds,
        String field,
        LocalDate from,
        LocalDate to
    ) {
        try {
            Expression<LocalDate> expr = root.get(field).as(LocalDate.class);
            preds.add(cb.between(expr, from, to));
        } catch (IllegalArgumentException ignored) {
            // field doesn't exist — skip
        }
    }


    private static Specification<DefendantAccountEntity> fetchJoins() {
        return (root, query, cb) -> {

            Class<?> rt = query.getResultType();

            if (rt != Long.class && rt != long.class) {
                try {
                    root.fetch(DefendantAccountEntity_.PARTIES, JoinType.LEFT)
                        .fetch(DefendantAccountPartiesEntity_.PARTY, JoinType.LEFT);
                } catch (IllegalArgumentException ignored) {
                    ignored.getMessage();
                }

                try {
                    root.fetch(DefendantAccountEntity_.ENFORCING_COURT, JoinType.LEFT);
                } catch (IllegalArgumentException ignored) {
                    ignored.getMessage();
                }

                try {
                    root.fetch(DefendantAccountEntity_.LAST_HEARING_COURT, JoinType.LEFT);
                } catch (IllegalArgumentException ignored) {
                    ignored.getMessage();
                }

                query.distinct(true);
            }
            return cb.conjunction();
        };
    }
}