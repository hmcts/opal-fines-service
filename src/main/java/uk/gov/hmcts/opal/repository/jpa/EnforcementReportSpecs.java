package uk.gov.hmcts.opal.repository.jpa;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity_.FINE_REGISTRATION_DATE;
import static uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity_.DEFENDANT_ACCOUNT_ID;
import static uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity_.POSTED_DATE;
import static uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity_.RESULT_ID;
import static uk.gov.hmcts.opal.util.DateTimeUtils.endOf;
import static uk.gov.hmcts.opal.util.DateTimeUtils.startOf;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.service.report.ReportEnforcementMode;
import uk.gov.hmcts.opal.service.report.ReportFiltersDto;

public class EnforcementReportSpecs {

    public static final String REGISTRATION_OF_FINE = "REGF";

    public static Specification<DefendantAccountEntity> enforcementSpec(ReportFiltersDto f) {
        return (root, query, cb) -> {

            ReportEnforcementMode mode = f.getReportEnforcementMode();
            if (mode == null) {
                return cb.conjunction();
            }

            switch (mode) {
                case ALL -> {
                    if (f.getEnforcementDateFrom() != null || f.getEnforcementDateTo() != null) {
                        Subquery<Long> sq = requireNonNull(query).subquery(Long.class);
                        Root<EnforcementEntity> ea = sq.from(EnforcementEntity.class);
                        List<Predicate> where = new ArrayList<>();
                        selectExistsWhereDefendantAccountIdMatches(root, cb, sq, ea);
                        where.add(cb.equal(ea.get(DEFENDANT_ACCOUNT_ID), root.get(DEFENDANT_ACCOUNT_ID)));
                        Expression<LocalDateTime> posted = ea.get(POSTED_DATE).as(LocalDateTime.class);
                        if (f.getEnforcementDateFrom() != null && f.getEnforcementDateTo() != null) {
                            where.add(cb.between(posted, startOf(f.getEnforcementDateFrom()),
                                endOf(f.getEnforcementDateTo())));
                        } else if (f.getEnforcementDateFrom() != null) {
                            where.add(cb.greaterThanOrEqualTo(posted, startOf(f.getEnforcementDateFrom())));
                        } else {
                            where.add(cb.lessThanOrEqualTo(posted, endOf(f.getEnforcementDateTo())));
                        }
                        sq.where(where.toArray(new Predicate[0]));
                        return cb.exists(sq);
                    }
                    return cb.conjunction();
                }

                // LAST_ACTION mode: ensure latest postedDate satisfies optional date range,
                // ensure an enforcement exists
                case LAST_ACTION -> {
                    List<Predicate> preds = new ArrayList<>();

                    // subquery returning greatest(postedDate) for this account
                    Subquery<LocalDateTime> maxDateSq = requireNonNull(query).subquery(LocalDateTime.class);
                    Root<EnforcementEntity> eaMax = maxDateSq.from(EnforcementEntity.class);

                    // cast postedDate to LocalDateTime before taking greatest
                    Expression<LocalDateTime> postedForMax = eaMax.get(POSTED_DATE).as(LocalDateTime.class);
                    maxDateSq.select(cb.greatest(postedForMax));
                    maxDateSq.where(cb.equal(eaMax.get(DEFENDANT_ACCOUNT_ID), root.get(DEFENDANT_ACCOUNT_ID)));

                    if (f.getLastActionDateFrom() != null) {
                        preds.add(cb.greaterThanOrEqualTo(maxDateSq, startOf(f.getLastActionDateFrom())));
                    }
                    if (f.getLastActionDateTo() != null) {
                        preds.add(cb.lessThanOrEqualTo(maxDateSq, endOf(f.getLastActionDateTo())));
                    }

                    // ensure at least one enforcement exists for this account
                    Subquery<Long> existsSq = query.subquery(Long.class);
                    Root<EnforcementEntity> eaExists = existsSq.from(EnforcementEntity.class);
                    selectExistsWhereDefendantAccountIdMatches(root, cb, existsSq, eaExists);
                    preds.add(cb.exists(existsSq));

                    return cb.and(preds.toArray(new Predicate[0]));
                }

                case REGF -> {
                    if (f.getRegfDateFrom() == null && f.getRegfDateTo() == null) {
                        Subquery<Long> sq = requireNonNull(query).subquery(Long.class);
                        Root<EnforcementEntity> ea = sq.from(EnforcementEntity.class);
                        selectExistsWhereDefendantAccountIdMatches(root, cb, sq, ea);
                        cb.equal(ea.get(RESULT_ID), REGISTRATION_OF_FINE);
                        return cb.or(cb.exists(sq), cb.isNotNull(root.get(FINE_REGISTRATION_DATE)));
                    } else {
                        Subquery<Long> sq = requireNonNull(query).subquery(Long.class);
                        Root<EnforcementEntity> ea = sq.from(EnforcementEntity.class);
                        List<Predicate> where = new ArrayList<>();
                        selectExistsWhereDefendantAccountIdMatches(root, cb, sq, ea);
                        where.add(cb.equal(ea.get(RESULT_ID), REGISTRATION_OF_FINE));

                        Expression<LocalDateTime> posted = ea.get(POSTED_DATE).as(LocalDateTime.class);

                        if (f.getRegfDateFrom() != null) {
                            where.add(cb.greaterThanOrEqualTo(posted, startOf(f.getRegfDateFrom())));
                        }
                        if (f.getRegfDateTo() != null) {
                            where.add(cb.lessThanOrEqualTo(posted, endOf(f.getRegfDateTo())));
                        }
                        sq.where(where.toArray(new Predicate[0]));
                        return cb.exists(sq);
                    }
                }

                case NOT_UNDER_ENFORCEMENT -> {
                    Subquery<Long> sq = requireNonNull(query).subquery(Long.class);
                    Root<EnforcementEntity> ea = sq.from(EnforcementEntity.class);
                    selectExistsWhereDefendantAccountIdMatches(root, cb, sq, ea);
                    return cb.not(cb.exists(sq));
                }
            }
            return cb.conjunction();
        };
    }

    private static void selectExistsWhereDefendantAccountIdMatches(Root<DefendantAccountEntity> root,
        CriteriaBuilder cb, Subquery<Long> sq,
        Root<EnforcementEntity> ea) {
        sq.select(cb.literal(1L)); // constant value for EXISTS subquery
        sq.where(cb.equal(ea.get(DEFENDANT_ACCOUNT_ID), root.get(DEFENDANT_ACCOUNT_ID)));
    }
}
