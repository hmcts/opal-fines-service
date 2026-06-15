package uk.gov.hmcts.opal.repository.jpa;


import static uk.gov.hmcts.opal.util.DateTimeUtils.endOf;
import static uk.gov.hmcts.opal.util.DateTimeUtils.startOf;
import static uk.gov.hmcts.opal.util.NumberUtils.toLong;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;

public final class ReportInstanceSpecs {

    private ReportInstanceSpecs() {
    }

    public static Specification<ReportInstanceEntity> build(
        LocalDate fromDate,
        LocalDate toDate,
        Integer userId,
        String reportId,
        List<Long> businessUnitIds) {

        return Specification
            .where(createdTimestampFrom(startOf(fromDate)))
            .and(createdTimestampTo(endOf(toDate)))
            .and(requestedByEquals(toLong(userId)))
            .and(reportIdEquals(reportId))
            .and(hasAnyBusinessUnitIn(businessUnitIds));

    }

    public static Specification<ReportInstanceEntity> createdTimestampFrom(
        LocalDateTime fromDateTime) {

        return (root, query, cb) ->
            fromDateTime == null
                ? null
                : cb.greaterThanOrEqualTo(root.get("createdTimestamp"), fromDateTime);
    }

    public static Specification<ReportInstanceEntity> createdTimestampTo(
        LocalDateTime toDateTime) {

        return (root, query, cb) ->
            toDateTime == null
                ? null
                : cb.lessThanOrEqualTo(root.get("createdTimestamp"), toDateTime);
    }

    public static Specification<ReportInstanceEntity> requestedByEquals(
        Long requestedBy) {

        return (root, query, cb) ->
            requestedBy == null
                ? null
                : cb.equal(root.get("requestedBy"), requestedBy);
    }

    public static Specification<ReportInstanceEntity> reportIdEquals(
        String reportId) {

        return (root, query, cb) ->
            reportId == null || reportId.isBlank()
                ? null
                : cb.equal(root.get("reportId"), reportId);
    }

    public static Specification<ReportInstanceEntity> hasAnyBusinessUnitIn(List<Long> businessUnitIds) {
        return (root, query, cb) -> {
            List<Short> smallintBusinessUnitIds = businessUnitIds == null
                ? List.of()
                : businessUnitIds.stream()
                    .filter(Objects::nonNull)
                    .map(ReportInstanceSpecs::toSmallint)
                    .distinct()
                    .toList();

            if (smallintBusinessUnitIds.isEmpty()) {
                return null;
            }

            Predicate[] predicates = smallintBusinessUnitIds.stream()
                .map(businessUnitId -> containsBusinessUnit(root, cb, businessUnitId))
                .toArray(Predicate[]::new);

            return cb.or(predicates);
        };
    }

    private static Predicate containsBusinessUnit(
        Root<ReportInstanceEntity> root,
        CriteriaBuilder cb,
        Short businessUnitId) {

        Expression<String> businessUnitIds = cb.concat(
            cb.concat(",", cb.function("array_to_string", String.class, root.get("businessUnit"), cb.literal(","))),
            ","
        );

        // Comma boundaries prevent partial matches, e.g. business unit 20 matching 120.
        return cb.like(businessUnitIds, "%," + businessUnitId + ",%");
    }

    private static Short toSmallint(Long value) {
        if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            throw new IllegalArgumentException("businessUnitId is outside PostgreSQL smallint range: " + value);
        }

        return value.shortValue();
    }
}
