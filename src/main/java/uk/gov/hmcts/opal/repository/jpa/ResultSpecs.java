package uk.gov.hmcts.opal.repository.jpa;

import org.hibernate.query.criteria.JpaExpression;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.entity.result.ResultEntity_;
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;

import java.util.List;
import java.util.Optional;

@Component
public class ResultSpecs extends EntitySpecs<ResultEntity> {

    public Specification<ResultEntity> findBySearchCriteria(ResultSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getResultId()).map(ResultSpecs::likeResultId),
            notBlank(criteria.getResultTitle()).map(ResultSpecs::likeResultTitle),
            notBlank(criteria.getResultTitleCy()).map(ResultSpecs::likeResultTitleCy),
            notBlank(criteria.getResultType()).map(ResultSpecs::likeResultType),
            numericShort(criteria.getImpositionAllocationPriority())
                .map(ResultSpecs::equalsImpositionAllocationPriority),
            notBlank(criteria.getImpositionCreditor()).map(ResultSpecs::likeImpositionCreditor)
        ));
    }

    public Specification<ResultEntity> referenceDataFilter(Optional<String> filter) {
        return Specification.allOf(specificationList(
            filter.filter(s -> !s.isBlank()).map(this::likeAnyResult)
        ));
    }

    public Specification<ResultEntity> referenceDataByIds(
        Optional<List<String>> resultIds,
        Boolean active,
        Boolean manualEnforcement,
        Boolean generatesHearing,
        Boolean enforcement,
        Boolean enforcementOverride) {

        return Specification.allOf(specificationList(
            // optional resultIds clause
            resultIds.map(ResultSpecs::equalsAnyResultId),

            Optional.ofNullable(active).map(ResultSpecs::hasActive),
            Optional.ofNullable(manualEnforcement).map(ResultSpecs::hasManualEnforcementOnly),
            Optional.ofNullable(generatesHearing).map(ResultSpecs::hasGeneratesHearing),
            Optional.ofNullable(enforcement).map(ResultSpecs::hasEnforcement),
            Optional.ofNullable(enforcementOverride).map(ResultSpecs::hasEnforcementOverride)

        ));
    }

    public static Specification<ResultEntity> equalsAnyResultId(List<String> resultIds) {
        return (root, query, builder) -> root.get(ResultEntity_.resultId).in(resultIds);
    }

    public static Specification<ResultEntity> likeResultId(String resultId) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(ResultEntity_.resultId), builder, resultId);
    }

    public static Specification<ResultEntity> likeResultTitle(String resultTitle) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultEntity_.resultTitle), builder,
                                                               resultTitle);
    }

    public static Specification<ResultEntity> likeResultTitleCy(String resultTitleCy) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultEntity_.resultTitleCy), builder,
                                                               resultTitleCy);
    }

    public static Specification<ResultEntity> likeResultType(String resultType) {
        return (root, query, builder) -> likeWildcardPredicate(
            ((JpaExpression<?>) root.get(ResultEntity_.resultType)).cast(String.class), builder, resultType);
    }

    public static Specification<ResultEntity>
        equalsImpositionAllocationPriority(Short impositionAllocationPriority) {
        return (root, query, builder) -> builder.equal(root.get(ResultEntity_.impositionAllocationPriority),
                                                       impositionAllocationPriority);
    }

    public static Specification<ResultEntity> likeImpositionCreditor(String impositionCreditor) {
        return (root, query, builder) -> likeWildcardPredicate(
            ((JpaExpression<?>) root.get(ResultEntity_.impositionCreditor)).cast(String.class), builder,
            impositionCreditor);
    }

    public Specification<ResultEntity> likeAnyResult(String filter) {
        return Specification.anyOf(
            likeResultId(filter),
            likeResultTitle(filter),
            likeResultTitleCy(filter)
        );
    }

    public static Specification<ResultEntity> hasActive(boolean active) {
        return (root, query, builder) -> builder.equal(root.get(
            ResultEntity_.active), active);
    }

    public static Specification<ResultEntity> hasManualEnforcementOnly(boolean manualEnforcement) {
        return (root, query, builder) -> builder.equal(root.get(
            ResultEntity_.manualEnforcement), manualEnforcement);
    }

    public static Specification<ResultEntity> hasGeneratesHearing(boolean generatesHearing) {
        return (root, query, builder) -> builder.equal(root.get(
            ResultEntity_.generatesHearing), generatesHearing);
    }

    public static Specification<ResultEntity> hasEnforcement(boolean hasEnforcement) {
        return (root, query, builder) -> builder.equal(root.get(
            ResultEntity_.enforcement), hasEnforcement);
    }

    public static Specification<ResultEntity> hasEnforcementOverride(boolean enforcementOverride) {
        return (root, query, builder) -> builder.equal(root.get(ResultEntity_.enforcementOverride),
            enforcementOverride);
    }
}
