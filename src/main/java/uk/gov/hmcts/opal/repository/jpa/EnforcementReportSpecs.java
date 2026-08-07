package uk.gov.hmcts.opal.repository.jpa;

import static uk.gov.hmcts.opal.service.report.ReportEnforcementMode.ALL;
import static uk.gov.hmcts.opal.service.report.ReportEnforcementMode.REGF;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.report.operation.OperationReportByEnforcementFiltersDto;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity_;
import uk.gov.hmcts.opal.service.report.ReportEnforcementMode;

public class EnforcementReportSpecs {

    public static Specification<EnforcementEntity> build(OperationReportByEnforcementFiltersDto filters) {
        return (root, query, cb) -> {
            Join<EnforcementEntity, DefendantAccountEntity> account =
                root.join(EnforcementEntity_.DEFENDANT_ACCOUNT);
            List<Predicate> predicates = modePredicates(root, query, cb, filters);
            predicates.add(OperationReportSpecs.accountFiltersByEnforcement(account, query, cb, filters));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static List<Predicate> modePredicates(Root<EnforcementEntity> root, CriteriaQuery<?> query,
        CriteriaBuilder builder, OperationReportByEnforcementFiltersDto filters) {

        return switch (enforcementMode(filters)) {
            case ALL -> allPredicates(root, builder, filters);
            case REGF -> regfPredicates(root, builder, filters);
            case LAST_ACTION -> lastActionPredicates(root, query, builder, filters);
            default -> List.of(builder.disjunction());
        };
    }

    private static ReportEnforcementMode enforcementMode(OperationReportByEnforcementFiltersDto filters) {
        return filters.getReportEnforcementMode() == null ? ALL : filters.getReportEnforcementMode();
    }

    private static List<Predicate> allPredicates(Root<EnforcementEntity> root, CriteriaBuilder builder,
        OperationReportByEnforcementFiltersDto filters) {

        return postedBetween(root, builder, filters.getEnforcementDateFrom(), filters.getEnforcementDateTo());
    }

    private static List<Predicate> regfPredicates(Root<EnforcementEntity> root, CriteriaBuilder builder,
        OperationReportByEnforcementFiltersDto filters) {

        List<Predicate> predicates = postedBetween(root, builder, filters.getRegfDateFrom(), filters.getRegfDateTo());
        predicates.add(EnforcementSpecs.resultIdPredicate(root, builder, REGF.name()));
        return predicates;
    }

    private static List<Predicate> lastActionPredicates(Root<EnforcementEntity> root,
        CriteriaQuery<?> query, CriteriaBuilder builder, OperationReportByEnforcementFiltersDto filters) {

        String action = filters.getEnforcementAction();
        if (action == null) {
            throw new IllegalArgumentException("enforcementAction is required for LAST_ACTION");
        }

        List<Predicate> predicates = postedAfterAndBefore(
            root,
            builder,
            filters.getLastActionDateFrom(),
            filters.getLastActionDateTo() == null ? null : filters.getLastActionDateTo().plusDays(1));
        predicates.add(EnforcementSpecs.resultIdPredicate(root, builder, action));
        predicates.add(EnforcementSpecs.latestActionPredicate(root, query, builder));
        return predicates;
    }

    private static List<Predicate> postedBetween(Root<EnforcementEntity> root, CriteriaBuilder builder,
        LocalDate from, LocalDate to) {

        List<Predicate> predicates = new ArrayList<>();
        if (from != null) {
            predicates.add(EnforcementSpecs.postedFromPredicate(root, builder, from));
        }
        if (to != null) {
            predicates.add(EnforcementSpecs.postedToPredicate(root, builder, to));
        }
        return predicates;
    }

    private static List<Predicate> postedAfterAndBefore(Root<EnforcementEntity> root, CriteriaBuilder builder,
        LocalDate fromInclusive, LocalDate toExclusive) {

        List<Predicate> predicates = new ArrayList<>();
        if (fromInclusive != null) {
            predicates.add(EnforcementSpecs.postedFromPredicate(root, builder, fromInclusive));
        }
        if (toExclusive != null) {
            predicates.add(EnforcementSpecs.postedBeforePredicate(root, builder, toExclusive));
        }
        return predicates;
    }
}
