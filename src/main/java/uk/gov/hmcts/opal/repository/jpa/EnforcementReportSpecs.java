package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.service.report.ReportEnforcementMode;
import uk.gov.hmcts.opal.service.report.ReportFiltersDto;

public class EnforcementReportSpecs {

    public static Specification<EnforcementEntity> build(ReportFiltersDto filters) {
        return (root, query, cb) -> {

            Join<EnforcementEntity, DefendantAccountEntity> account =
                root.join("defendantAccount");
            List<Predicate> preds = new ArrayList<>();
            ReportEnforcementMode mode = filters.getReportEnforcementMode();

            if (mode != null) {
                switch (mode) {
                    case ALL -> {
                        if (filters.getEnforcementDateFrom() != null) {
                            preds.add(cb.greaterThanOrEqualTo(
                                root.get("postedDate"),
                                startOf(filters.getEnforcementDateFrom())
                            ));
                        }
                        if (filters.getEnforcementDateTo() != null) {
                            preds.add(cb.lessThanOrEqualTo(
                                root.get("postedDate"),
                                endOf(filters.getEnforcementDateTo())
                            ));
                        }
                    }

                    case REGF -> {
                        preds.add(cb.equal(root.get("resultId"), "REGF"));

                        if (filters.getRegfDateFrom() != null) {
                            preds.add(cb.greaterThanOrEqualTo(
                                root.get("postedDate"),
                                startOf(filters.getRegfDateFrom())
                            ));
                        }
                        if (filters.getRegfDateTo() != null) {
                            preds.add(cb.lessThanOrEqualTo(
                                root.get("postedDate"),
                                endOf(filters.getRegfDateTo())
                            ));
                        }
                    }

                    case LAST_ACTION -> {
                        Subquery<LocalDateTime> maxDateSq = query.subquery(LocalDateTime.class);
                        Root<EnforcementEntity> sub = maxDateSq.from(EnforcementEntity.class);

                        maxDateSq.select(cb.greatest(sub.get("postedDate").as(LocalDateTime.class)));
                        maxDateSq.where(
                            cb.equal(sub.get("defendantAccount"), root.get("defendantAccount"))
                        );
                        preds.add(cb.equal(root.get("postedDate"), maxDateSq));
                        if (filters.getLastActionDateFrom() != null) {
                            preds.add(cb.greaterThanOrEqualTo(
                                root.get("postedDate"),
                                startOf(filters.getLastActionDateFrom())
                            ));
                        }
                        if (filters.getLastActionDateTo() != null) {
                            preds.add(cb.lessThan(
                                root.get("postedDate"),
                                endOf(filters.getLastActionDateTo().plusDays(1))
                            ));
                        }
                        return cb.and(preds.toArray(new Predicate[0]));
                    }

                    default -> {
                        return cb.disjunction();
                    }
                }
            }
            preds.add(ReportSpecs.accountFilters(account, query, cb, filters));
            return cb.and(preds.toArray(new Predicate[0]));
        };
    }

    public static Specification<DefendantAccountEntity> notUnderEnforcement() {
        return (root, query, cb) -> {

            Subquery<Long> sq = query.subquery(Long.class);
            Root<EnforcementEntity> enforcement = sq.from(EnforcementEntity.class);
            sq.select(cb.literal(1L)); //Ensure at least one exists
            sq.where(
                cb.equal(
                    enforcement.get("defendantAccount").get("defendantAccountId"),
                    root.get("defendantAccountId")
                )
            );
            return cb.not(cb.exists(sq));
        };
    }

    private static LocalDateTime startOf(LocalDate d) {
        return d.atStartOfDay();
    }

    private static LocalDateTime endOf(LocalDate d) {
        return d.atTime(LocalTime.MAX);
    }
}