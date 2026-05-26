package uk.gov.hmcts.opal.repository.jpa;

import static uk.gov.hmcts.opal.service.report.ReportEnforcementMode.ALL;
import static uk.gov.hmcts.opal.service.report.ReportEnforcementMode.REGF;
import static uk.gov.hmcts.opal.util.DateTimeUtils.endOf;
import static uk.gov.hmcts.opal.util.DateTimeUtils.startOf;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity_;
import uk.gov.hmcts.opal.service.report.ReportEnforcementMode;
import uk.gov.hmcts.opal.service.report.OperationReportByEnforcementFiltersDto;

public class EnforcementReportSpecs {

    public static Specification<EnforcementEntity> build(OperationReportByEnforcementFiltersDto filters) {
        return (root, query, cb) -> {

            Join<EnforcementEntity, DefendantAccountEntity> account =
                root.join(EnforcementEntity_.DEFENDANT_ACCOUNT);
            List<Predicate> preds = new ArrayList<>();
            ReportEnforcementMode mode = filters.getReportEnforcementMode();
            if (mode == null) {
                mode = ALL;
            }
            switch (mode) {
                case ALL -> {
                    if (filters.getEnforcementDateFrom() != null) {
                        preds.add(cb.greaterThanOrEqualTo(
                            root.get(EnforcementEntity_.POSTED_DATE),
                            startOf(filters.getEnforcementDateFrom())
                        ));
                    }
                    if (filters.getEnforcementDateTo() != null) {
                        preds.add(cb.lessThanOrEqualTo(
                            root.get(EnforcementEntity_.POSTED_DATE),
                            endOf(filters.getEnforcementDateTo())
                        ));
                    }
                }

                case REGF -> {
                    preds.add(cb.equal(root.get(EnforcementEntity_.RESULT_ID), REGF.name()));

                    if (filters.getRegfDateFrom() != null) {
                        preds.add(cb.greaterThanOrEqualTo(
                            root.get(EnforcementEntity_.POSTED_DATE),
                            startOf(filters.getRegfDateFrom())
                        ));
                    }
                    if (filters.getRegfDateTo() != null) {
                        preds.add(cb.lessThanOrEqualTo(
                            root.get(EnforcementEntity_.POSTED_DATE),
                            endOf(filters.getRegfDateTo())
                        ));
                    }
                }

                case LAST_ACTION -> {
                    String action = filters.getEnforcementAction();
                    if (action == null) {
                        throw new IllegalArgumentException("enforcementAction is required for LAST_ACTION");
                    }
                    preds.add(cb.equal(root.get(EnforcementEntity_.RESULT_ID), action));
                    Subquery<LocalDateTime> maxDateSq = query.subquery(LocalDateTime.class);
                    Root<EnforcementEntity> sub = maxDateSq.from(EnforcementEntity.class);

                    maxDateSq.select(cb.greatest(sub.get(EnforcementEntity_.POSTED_DATE).as(LocalDateTime.class)));
                    maxDateSq.where(
                        cb.equal(sub.get(EnforcementEntity_.DEFENDANT_ACCOUNT),
                            root.get(EnforcementEntity_.DEFENDANT_ACCOUNT))
                    );
                    preds.add(cb.equal(root.get(EnforcementEntity_.POSTED_DATE), maxDateSq));
                    if (filters.getLastActionDateFrom() != null) {
                        preds.add(cb.greaterThanOrEqualTo(
                            root.get(EnforcementEntity_.POSTED_DATE),
                            startOf(filters.getLastActionDateFrom())
                        ));
                    }
                    if (filters.getLastActionDateTo() != null) {
                        preds.add(cb.lessThan(
                            root.get(EnforcementEntity_.POSTED_DATE),
                            endOf(filters.getLastActionDateTo().plusDays(1))
                        ));
                    }
                }

                default -> {
                    return cb.disjunction();
                }
            }
            preds.add(ReportSpecs.accountFilters(account, query, cb, filters));
            return cb.and(preds.toArray(new Predicate[0]));
        };
    }
}