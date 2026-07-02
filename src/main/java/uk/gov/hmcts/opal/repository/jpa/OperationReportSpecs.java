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
import uk.gov.hmcts.opal.dto.ResultId;
import uk.gov.hmcts.opal.dto.report.operation.OperationReportByEnforcementFiltersDto;
import uk.gov.hmcts.opal.dto.report.operation.OperationReportByPaymentFiltersDto;
import uk.gov.hmcts.opal.dto.report.operation.OperationReportFiltersDto;
import uk.gov.hmcts.opal.dto.report.operation.PaymentReportMode;
import uk.gov.hmcts.opal.entity.PartyEntity_;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity_;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity_;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionEntity_;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity_;
import uk.gov.hmcts.opal.entity.search.SearchDefendantAccount_;
import uk.gov.hmcts.opal.service.report.ReportEnforcementMode;

@AllArgsConstructor
public final class OperationReportSpecs {

    public static Specification<DefendantAccountEntity> build(OperationReportFiltersDto filters) {
        return Specification.where(fetchJoins())
            .and(accountFiltersSpec(filters));
    }

    public static Specification<DefendantAccountEntity> accountFiltersSpec(
        OperationReportFiltersDto filters
    ) {
        if (filters instanceof OperationReportByEnforcementFiltersDto enforcementFilters) {
            return (root, query, cb) ->
                accountFiltersByEnforcement(root, query, cb, enforcementFilters);
        }
        if (filters instanceof OperationReportByPaymentFiltersDto paymentFilters) {
            return (root, query, cb) ->
                accountFiltersByPayment(root, query, cb, paymentFilters);
        }
        throw new IllegalArgumentException(
            "Unsupported filters type: " + (filters == null ? "null" : filters.getClass().getName())
        );
    }

    public static Specification<DefendantAccountEntity> defendantAccountIdsIn(List<Long> accountIds) {
        return (root, query, cb) -> {
            if (accountIds == null || accountIds.isEmpty()) {
                return cb.disjunction();
            }
            return root.get(DefendantAccountEntity_.DEFENDANT_ACCOUNT_ID).in(accountIds);
        };
    }

    public static Predicate accountFiltersByEnforcement(
        From<?, DefendantAccountEntity> root,
        CriteriaQuery<?> query,
        CriteriaBuilder cb,
        OperationReportByEnforcementFiltersDto filters
    ) {
        List<Predicate> predicates = new ArrayList<>();
        addCommonFilters(root, query, cb, filters, predicates);
        addNotUnderEnforcementFilter(root, cb, filters, predicates);
        return combinePredicates(cb, predicates);
    }

    public static Predicate accountFiltersByPayment(
        From<?, DefendantAccountEntity> root,
        CriteriaQuery<?> query,
        CriteriaBuilder cb,
        OperationReportByPaymentFiltersDto filters
    ) {
        List<Predicate> predicates = new ArrayList<>();
        addCommonFilters(root, query, cb, filters, predicates);
        if (filters.getReportMode().equals(PaymentReportMode.SINCE_DATE)) {
            addIsPaymentMadeFilter(root, query, cb, filters, predicates);
        }
        addLastEnforcementFilter(root, cb, filters, predicates);
        return combinePredicates(cb, predicates);
    }

    private static void addCommonFilters(
        From<?, DefendantAccountEntity> root,
        CriteriaQuery<?> query,
        CriteriaBuilder cb,
        OperationReportFiltersDto filters,
        List<Predicate> predicates
    ) {
        addBusinessUnitFilter(root, filters, predicates);
        addAccountTypeFilter(root, cb, filters, predicates);
        addParentGuardianFilter(root, query, cb, filters, predicates);
        addCollectionOrderFilter(root, cb, filters, predicates);
        addAccountStatusFilter(root, cb, filters, predicates);
        addBalanceFilter(root, cb, filters, predicates);
        addNameRangeFilter(root, cb, filters, predicates);
        addNext7DaysFilter(root, query, cb, filters, predicates);
    }

    private static Predicate combinePredicates(CriteriaBuilder cb, List<Predicate> predicates) {
        return predicates.isEmpty()
            ? cb.conjunction()
            : cb.and(predicates.toArray(new Predicate[0]));
    }

    private static void addBusinessUnitFilter(
        From<?, DefendantAccountEntity> root,
        OperationReportFiltersDto filters,
        List<Predicate> predicates
    ) {
        if (filters.getBusinessUnitIds() != null && !filters.getBusinessUnitIds().isEmpty()) {
            predicates.add(
                root.get(DefendantAccountEntity_.BUSINESS_UNIT)
                    .get(SearchDefendantAccount_.BUSINESS_UNIT_ID)
                    .in(filters.getBusinessUnitIds())
            );
        }
    }

    private static void addIsPaymentMadeFilter(
        From<?, DefendantAccountEntity> root,
        CriteriaQuery<?> query,
        CriteriaBuilder cb,
        OperationReportByPaymentFiltersDto filters,
        List<Predicate> predicates
    ) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<DefendantTransactionEntity> transaction = subquery.from(DefendantTransactionEntity.class);
        subquery.select(transaction.get(DefendantTransactionEntity_.DEFENDANT_ACCOUNT_ID))
            .where(
                DefendantTransactionSpecs.paymentMadeOnOrAfterDatePredicate(
                    transaction, cb, filters.getSinceDate()
                )
            );
        Predicate hasPayment = root.get(DefendantAccountEntity_.DEFENDANT_ACCOUNT_ID).in(subquery);
        predicates.add(Boolean.TRUE.equals(filters.getIsPaymentMade()) ? hasPayment : cb.not(hasPayment));
    }

    private static void addLastEnforcementFilter(
        From<?, DefendantAccountEntity> root,
        CriteriaBuilder cb,
        OperationReportByPaymentFiltersDto filters,
        List<Predicate> predicates
    ) {
        ResultId lastEnforcementFilter = filters.getSinceLastEnforcementAction();
        if (lastEnforcementFilter != null) {
            predicates.add(
                cb.equal(root.get(DefendantAccountEntity_.LAST_ENFORCEMENT), lastEnforcementFilter.value())
            );
        }
    }

    private static void addAccountTypeFilter(
        From<?, DefendantAccountEntity> root,
        CriteriaBuilder cb,
        OperationReportFiltersDto filters,
        List<Predicate> predicates
    ) {
        if (!(Boolean.TRUE.equals(filters.getIncludeAdult())
            || Boolean.TRUE.equals(filters.getIncludeYouth())
            || Boolean.TRUE.equals(filters.getIncludeCompany()))) {
            return;
        }

        Join<?, ?> link = root.join(DefendantAccountEntity_.PARTIES, JoinType.LEFT);
        Join<?, ?> party = link.join(DefendantAccountPartiesEntity_.PARTY, JoinType.LEFT);

        List<Predicate> typePredicates = new ArrayList<>();

        if (Boolean.TRUE.equals(filters.getIncludeAdult())) {
            typePredicates.add(cb.greaterThanOrEqualTo(party.get(PartyEntity_.AGE), ADULT_AGE));
        }
        if (Boolean.TRUE.equals(filters.getIncludeYouth())) {
            typePredicates.add(cb.lessThan(party.get(PartyEntity_.AGE), ADULT_AGE));
        }
        if (Boolean.TRUE.equals(filters.getIncludeCompany())) {
            typePredicates.add(cb.isTrue(party.get(PartyEntity_.ORGANISATION)));
        }

        predicates.add(cb.or(typePredicates.toArray(new Predicate[0])));
    }

    private static void addParentGuardianFilter(
        From<?, DefendantAccountEntity> root,
        CriteriaQuery<?> query,
        CriteriaBuilder cb,
        OperationReportFiltersDto filters,
        List<Predicate> predicates
    ) {
        if (!Boolean.TRUE.equals(filters.getOnlyAccountsWithParentGuardian())) {
            return;
        }
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<DefendantAccountPartiesEntity> dap = subquery.from(DefendantAccountPartiesEntity.class);

        subquery.select(cb.literal(1L));
        subquery.where(
            cb.equal(
                dap.get(DefendantAccountPartiesEntity_.DEFENDANT_ACCOUNT)
                    .get(DefendantAccountEntity_.DEFENDANT_ACCOUNT_ID),
                root.get(DefendantAccountEntity_.DEFENDANT_ACCOUNT_ID)
            ),
            cb.equal(dap.get(DefendantAccountPartiesEntity_.ASSOCIATION_TYPE), PARENT_GUARDIAN)
        );
        predicates.add(cb.exists(subquery));
    }

    private static void addCollectionOrderFilter(
        From<?, DefendantAccountEntity> root,
        CriteriaBuilder cb,
        OperationReportFiltersDto filters,
        List<Predicate> predicates
    ) {
        if (filters.getCollectionOrderChoice() == null) {
            return;
        }
        if (WITH.equals(filters.getCollectionOrderChoice())) {
            predicates.add(cb.isTrue(root.get(DefendantAccountEntity_.COLLECTION_ORDER)));
        } else if (WITHOUT.equals(filters.getCollectionOrderChoice())) {
            predicates.add(cb.isFalse(root.get(DefendantAccountEntity_.COLLECTION_ORDER)));
        }
    }

    private static void addAccountStatusFilter(
        From<?, DefendantAccountEntity> root,
        CriteriaBuilder cb,
        OperationReportFiltersDto filters,
        List<Predicate> predicates
    ) {
        if (filters.getAccountStatus() == null) {
            return;
        }
        if (LIVE.equals(filters.getAccountStatus())) {
            predicates.add(cb.and(
                cb.greaterThan(root.get(DefendantAccountEntity_.ACCOUNT_BALANCE), cb.literal(0)),
                cb.isNull(root.get(DefendantAccountEntity_.COMPLETED_DATE))
            ));
        } else if (CLOSED.equals(filters.getAccountStatus())) {
            predicates.add(cb.or(
                cb.equal(root.get(DefendantAccountEntity_.ACCOUNT_BALANCE), cb.literal(0)),
                cb.isNotNull(root.get(DefendantAccountEntity_.COMPLETED_DATE))
            ));
        }
    }

    private static void addBalanceFilter(
        From<?, DefendantAccountEntity> root,
        CriteriaBuilder cb,
        OperationReportFiltersDto filters,
        List<Predicate> predicates
    ) {
        if (filters.getMinBalance() != null) {
            predicates.add(
                cb.greaterThanOrEqualTo(root.get(DefendantAccountEntity_.ACCOUNT_BALANCE), filters.getMinBalance())
            );
        }
        if (filters.getMaxBalance() != null) {
            predicates.add(
                cb.lessThanOrEqualTo(root.get(DefendantAccountEntity_.ACCOUNT_BALANCE), filters.getMaxBalance())
            );
        }
    }

    private static void addNameRangeFilter(
        From<?, DefendantAccountEntity> root,
        CriteriaBuilder cb,
        OperationReportFiltersDto filters,
        List<Predicate> predicates
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
            predicates.add(cb.greaterThanOrEqualTo(firstLetter, filters.getLowerNameRange().toLowerCase()));
        }
        if (filters.getUpperNameRange() != null) {
            predicates.add(cb.lessThanOrEqualTo(firstLetter, filters.getUpperNameRange().toLowerCase()));
        }
    }

    private static void addNext7DaysFilter(
        From<?, DefendantAccountEntity> root,
        CriteriaQuery<?> query,
        CriteriaBuilder cb,
        OperationReportFiltersDto filters,
        List<Predicate> predicates
    ) {
        if (!Boolean.TRUE.equals(filters.getFirstPaymentOrPayByInNext7Days())) {
            return;
        }
        LocalDate today = todayUk();
        LocalDate in7Days = todayPlusDaysUk(7);

        Subquery<Long> subquery = query.subquery(Long.class);
        Root<PaymentTermsEntity> paymentTerms = subquery.from(PaymentTermsEntity.class);

        subquery.select(cb.literal(1L));  // 'SELECT 1' TO CHECK EXISTS
        subquery.where(
            cb.equal(
                paymentTerms.get(PaymentTermsEntity_.DEFENDANT_ACCOUNT)
                    .get(DefendantAccountEntity_.DEFENDANT_ACCOUNT_ID),
                root.get(DefendantAccountEntity_.DEFENDANT_ACCOUNT_ID)
            ),
            cb.between(paymentTerms.get(PaymentTermsEntity_.EFFECTIVE_DATE), today, in7Days)
        );
        predicates.add(cb.exists(subquery));
    }

    private static void addNotUnderEnforcementFilter(
        From<?, DefendantAccountEntity> root,
        CriteriaBuilder cb,
        OperationReportByEnforcementFiltersDto filters,
        List<Predicate> predicates
    ) {
        if (ReportEnforcementMode.NOT_UNDER_ENFORCEMENT.equals(filters.getReportEnforcementMode())) {
            predicates.add(cb.isNull(root.get(DefendantAccountEntity_.LAST_ENFORCEMENT)));
        }
    }

    private static Specification<DefendantAccountEntity> fetchJoins() {
        return (root, query, cb) -> {
            Class<?> resultType = query.getResultType();
            if (resultType != Long.class && resultType != long.class) {
                safeFetch(() -> root.fetch(DefendantAccountEntity_.PARTIES, JoinType.LEFT)
                    .fetch(DefendantAccountPartiesEntity_.PARTY, JoinType.LEFT));
                safeFetch(() -> root.fetch(DefendantAccountEntity_.ENFORCING_COURT, JoinType.LEFT));
                safeFetch(() -> root.fetch(DefendantAccountEntity_.LAST_HEARING_COURT, JoinType.LEFT));
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }

    private static void safeFetch(Runnable fetch) {
        try {
            fetch.run();
        } catch (IllegalArgumentException ignored) {
            // Intentionally ignored: some queries cannot fetch these associations.
        }
    }
}