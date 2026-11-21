package uk.gov.hmcts.opal.repository.jpa;

import org.hibernate.query.criteria.JpaExpression;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.entity.result.ResultOpalEntity;
import uk.gov.hmcts.opal.entity.result.ResultOpalEntity_;
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;

import java.util.List;
import java.util.Optional;

@Component
public class ResultOpalSpecs extends EntitySpecs<ResultOpalEntity> {

    public Specification<ResultOpalEntity> findBySearchCriteria(ResultSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getResultId()).map(ResultOpalSpecs::likeResultId),
            notBlank(criteria.getResultTitle()).map(ResultOpalSpecs::likeResultTitle),
            notBlank(criteria.getResultTitleCy()).map(ResultOpalSpecs::likeResultTitleCy),
            notBlank(criteria.getResultType()).map(ResultOpalSpecs::likeResultType),
            numericShort(criteria.getImpositionAllocationPriority())
                .map(ResultOpalSpecs::equalsImpositionAllocationPriority),
            notBlank(criteria.getImpositionCreditor()).map(ResultOpalSpecs::likeImpositionCreditor)
        ));
    }

    public Specification<ResultOpalEntity> referenceDataFilter(Optional<String> filter) {
        return Specification.allOf(specificationList(
            filter.filter(s -> !s.isBlank()).map(this::likeAnyResult)
        ));
    }

    public Specification<ResultOpalEntity> referenceDataByIds(
        Optional<List<String>> resultIds,
        Boolean active,
        Boolean manualEnforcement,
        Boolean generatesHearing,
        Boolean enforcement,
        Boolean enforcementOverride) {

        return Specification.allOf(specificationList(
            // optional resultIds clause
            resultIds.map(ResultOpalSpecs::equalsAnyResultId),

            Optional.ofNullable(active).map(ResultOpalSpecs::hasActive),
            Optional.ofNullable(manualEnforcement).map(ResultOpalSpecs::hasManualEnforcementOnly),
            Optional.ofNullable(generatesHearing).map(ResultOpalSpecs::hasGeneratesHearing),
            Optional.ofNullable(enforcement).map(ResultOpalSpecs::hasEnforcement),
            Optional.ofNullable(enforcementOverride).map(ResultOpalSpecs::hasEnforcementOverride)

        ));
    }

    public static Specification<ResultOpalEntity> equalsAnyResultId(List<String> resultIds) {
        return (root, query, builder) -> root.get(ResultOpalEntity_.resultId).in(resultIds);
    }

    public static Specification<ResultOpalEntity> likeResultId(String resultId) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(ResultOpalEntity_.resultId), builder, resultId);
    }

    public static Specification<ResultOpalEntity> likeResultTitle(String resultTitle) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultOpalEntity_.resultTitle), builder,
                                                               resultTitle);
    }

    public static Specification<ResultOpalEntity> likeResultTitleCy(String resultTitleCy) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultOpalEntity_.resultTitleCy), builder,
                                                               resultTitleCy);
    }

    public static Specification<ResultOpalEntity> likeResultType(String resultType) {
        return (root, query, builder) -> likeWildcardPredicate(
            ((JpaExpression<?>) root.get(ResultOpalEntity_.resultType)).cast(String.class), builder, resultType);
    }

    public static Specification<ResultOpalEntity>
        equalsImpositionAllocationPriority(Short impositionAllocationPriority) {
        return (root, query, builder) -> builder.equal(root.get(ResultOpalEntity_.impositionAllocationPriority),
                                                       impositionAllocationPriority);
    }

    public static Specification<ResultOpalEntity> likeImpositionCreditor(String impositionCreditor) {
        return (root, query, builder) -> likeWildcardPredicate(
            ((JpaExpression<?>) root.get(ResultOpalEntity_.impositionCreditor)).cast(String.class), builder,
            impositionCreditor);
    }

    public Specification<ResultOpalEntity> likeAnyResult(String filter) {
        return Specification.anyOf(
            likeResultId(filter),
            likeResultTitle(filter),
            likeResultTitleCy(filter)
        );
    }

    public static Specification<ResultOpalEntity> hasActive(boolean active) {
        return (root, query, builder) -> builder.equal(root.get(
            ResultOpalEntity_.active), active);
    }

    public static Specification<ResultOpalEntity> hasManualEnforcementOnly(boolean manualEnforcement) {
        return (root, query, builder) -> builder.equal(root.get(
            ResultOpalEntity_.manualEnforcement), manualEnforcement);
    }

    public static Specification<ResultOpalEntity> hasGeneratesHearing(boolean generatesHearing) {
        return (root, query, builder) -> builder.equal(root.get(
            ResultOpalEntity_.generatesHearing), generatesHearing);
    }

    public static Specification<ResultOpalEntity> hasEnforcement(boolean hasEnforcement) {
        return (root, query, builder) -> builder.equal(root.get(
            ResultOpalEntity_.enforcement), hasEnforcement);
    }

    public static Specification<ResultOpalEntity> hasEnforcementOverride(boolean enforcementOverride) {
        return (root, query, builder) -> builder.equal(root.get(ResultOpalEntity_.enforcementOverride),
            enforcementOverride);
    }
}
