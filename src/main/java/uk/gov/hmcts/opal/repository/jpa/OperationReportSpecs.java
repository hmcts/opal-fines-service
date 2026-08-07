package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.ResultId;
import uk.gov.hmcts.opal.dto.report.operation.OperationReportByEnforcementFiltersDto;
import uk.gov.hmcts.opal.dto.report.operation.OperationReportByPaymentFiltersDto;
import uk.gov.hmcts.opal.dto.report.operation.OperationReportFiltersDto;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity_;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity_;
import uk.gov.hmcts.opal.service.report.ReportEnforcementMode;

public final class OperationReportSpecs {

    private OperationReportSpecs() {
    }

    public static Specification<DefendantAccountEntity> build(OperationReportFiltersDto filters) {
        return Specification.where(fetchJoins()).and(accountFiltersSpec(filters));
    }

    public static Specification<DefendantAccountEntity> accountFiltersSpec(OperationReportFiltersDto filters) {
        if (filters instanceof OperationReportByEnforcementFiltersDto enforcementFilters) {
            return (root, query, cb) -> enforcementFiltersPredicate(root, query, cb, enforcementFilters);
        }
        if (filters instanceof OperationReportByPaymentFiltersDto paymentFilters) {
            return (root, query, cb) -> paymentFiltersPredicate(root, query, cb, paymentFilters);
        }
        throw new IllegalArgumentException(
            "Unsupported filters type: " + (filters == null ? "null" : filters.getClass().getName()));
    }

    public static Predicate accountFiltersByEnforcement(From<?, DefendantAccountEntity> root, CriteriaQuery<?> query,
        CriteriaBuilder cb, OperationReportByEnforcementFiltersDto filters) {

        return enforcementFiltersPredicate(root, query, cb, filters);
    }

    private static Predicate enforcementFiltersPredicate(From<?, DefendantAccountEntity> root, CriteriaQuery<?> query,
        CriteriaBuilder cb, OperationReportByEnforcementFiltersDto filters) {

        return cb.and(predicateArray(
            Optional.of(commonFiltersPredicate(root, query, cb, filters)),
            optionalNotUnderEnforcementPredicate(root, cb, filters)
        ));
    }

    private static Predicate paymentFiltersPredicate(From<?, DefendantAccountEntity> root, CriteriaQuery<?> query,
        CriteriaBuilder cb, OperationReportByPaymentFiltersDto filters) {

        return cb.and(predicateArray(
            Optional.of(commonFiltersPredicate(root, query, cb, filters)),
            optionalPaymentMadePredicate(root, query, cb, filters),
            optionalLastEnforcementPredicate(root, cb, filters)
        ));
    }

    private static Predicate commonFiltersPredicate(From<?, DefendantAccountEntity> root, CriteriaQuery<?> query,
        CriteriaBuilder cb, OperationReportFiltersDto filters) {

        return cb.and(predicateArray(
            notNullOrEmpty(filters.getBusinessUnitIds())
                .map(ids -> DefendantAccountSpecs.businessUnitIdsInPredicate(root, ids)),
            hasAccountTypeFilter(filters)
                .map(ignore -> DefendantAccountSpecs.operationReportAccountTypesPredicate(root, cb, filters)),
            selected(filters.getOnlyAccountsWithParentGuardian())
                .map(ignore -> DefendantAccountSpecs.parentGuardianExistsPredicate(root, query, cb)),
            Optional.ofNullable(filters.getCollectionOrderChoice())
                .map(choice -> DefendantAccountSpecs.collectionOrderPredicate(root, cb, choice)),
            Optional.ofNullable(filters.getAccountStatus())
                .map(status -> DefendantAccountSpecs.accountStatusPredicate(root, cb, status)),
            Optional.ofNullable(filters.getMinBalance())
                .map(minBalance -> DefendantAccountSpecs.minBalancePredicate(root, cb, minBalance)),
            Optional.ofNullable(filters.getMaxBalance())
                .map(maxBalance -> DefendantAccountSpecs.maxBalancePredicate(root, cb, maxBalance)),
            hasNameRangeFilter(filters).map(ignore -> DefendantAccountSpecs.nameRangePredicate(
                root, cb, filters.getLowerNameRange(), filters.getUpperNameRange())),
            selected(filters.getFirstPaymentOrPayByInNext7Days())
                .map(ignore -> PaymentTermsSpecs.effectiveInNextDaysPredicate(root, query, cb, 7))
        ));
    }

    private static Optional<OperationReportFiltersDto> hasAccountTypeFilter(OperationReportFiltersDto filters) {
        return Optional.of(filters).filter(candidate -> Boolean.TRUE.equals(candidate.getIncludeAdult())
            || Boolean.TRUE.equals(candidate.getIncludeYouth())
            || Boolean.TRUE.equals(candidate.getIncludeCompany()));
    }

    private static Optional<OperationReportFiltersDto> hasNameRangeFilter(OperationReportFiltersDto filters) {
        return Optional.of(filters).filter(candidate -> notBlank(candidate.getLowerNameRange()).isPresent()
            || notBlank(candidate.getUpperNameRange()).isPresent());
    }

    private static Optional<Predicate> optionalNotUnderEnforcementPredicate(From<?, DefendantAccountEntity> root,
        CriteriaBuilder cb, OperationReportByEnforcementFiltersDto filters) {

        return Optional.ofNullable(filters.getReportEnforcementMode())
            .filter(ReportEnforcementMode.NOT_UNDER_ENFORCEMENT::equals)
            .map(ignore -> DefendantAccountSpecs.notUnderEnforcementPredicate(root, cb));
    }

    private static Optional<Predicate> optionalLastEnforcementPredicate(From<?, DefendantAccountEntity> root,
        CriteriaBuilder cb, OperationReportByPaymentFiltersDto filters) {

        return Optional.ofNullable(filters.getSinceLastEnforcementAction())
            .map(lastEnforcement -> DefendantAccountSpecs.lastEnforcementPredicate(root, cb, lastEnforcement));
    }

    private static Optional<Predicate> optionalPaymentMadePredicate(From<?, DefendantAccountEntity> root,
        CriteriaQuery<?> query, CriteriaBuilder cb, OperationReportByPaymentFiltersDto filters) {

        return Optional.ofNullable(filters.getIsPaymentMade()).map(isPaymentMade -> switch (filters.getReportMode()) {
                case SINCE_DATE -> paymentMadeOnOrAfterPredicate(
                    root, query, cb, filters.getSinceDate(), isPaymentMade);
                case WITH_REGF -> paymentMadeAfterEnforcementPredicate(
                    root, query, cb, ResultId.REGF.value(), true, isPaymentMade);
                case SINCE_LAST_ENFORCEMENT -> paymentMadeAfterEnforcementPredicate(
                    root, query, cb, filters.getSinceLastEnforcementAction().value(), false, isPaymentMade);
            });
    }

    private static Predicate paymentMadeOnOrAfterPredicate(From<?, DefendantAccountEntity> account,
        CriteriaQuery<?> query, CriteriaBuilder cb, LocalDate sinceDate, boolean isPaymentMade) {

        Predicate hasPayment =
            DefendantTransactionSpecs.accountHasPaymentFromPredicate(account, query, cb, sinceDate);
        return isPaymentMade ? hasPayment : cb.not(hasPayment);
    }

    private static Predicate paymentMadeAfterEnforcementPredicate(From<?, DefendantAccountEntity> account,
        CriteriaQuery<?> query, CriteriaBuilder cb, String resultId, boolean firstEnforcement, boolean isPaymentMade) {

        Predicate hasEnforcement = EnforcementSpecs.accountHasResultPredicate(account, query, cb, resultId);
        Predicate hasPaymentAfterEnforcement =
            DefendantTransactionSpecs.accountHasPaymentFromEnforcementPredicate(
                account, query, cb, resultId, firstEnforcement);
        return cb.and(
            hasEnforcement,
            isPaymentMade ? hasPaymentAfterEnforcement : cb.not(hasPaymentAfterEnforcement));
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

    @SafeVarargs
    private static Predicate[] predicateArray(Optional<Predicate>... optionalPredicates) {
        return Arrays.stream(optionalPredicates)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(Objects::nonNull)
            .toArray(Predicate[]::new);
    }

    private static Optional<Boolean> selected(Boolean candidate) {
        return Optional.ofNullable(candidate).filter(Boolean::booleanValue);
    }

    private static Optional<String> notBlank(String candidate) {
        return Optional.ofNullable(candidate).filter(value -> !value.isBlank());
    }

    private static <T> Optional<Collection<T>> notNullOrEmpty(Collection<T> collection) {
        return Optional.ofNullable(collection).filter(candidate -> !candidate.isEmpty());
    }

}
