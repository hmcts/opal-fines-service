package uk.gov.hmcts.opal.repository.jpa;

import org.hibernate.query.criteria.JpaExpression;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.entity.result.ResultLegacyEntity;
import uk.gov.hmcts.opal.entity.result.ResultLegacyEntity_;
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;

import java.util.List;
import java.util.Optional;

@Component
public class ResultLegacySpecs extends EntitySpecs<ResultLegacyEntity> {

    public Specification<ResultLegacyEntity> findBySearchCriteria(ResultSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getResultId()).map(ResultLegacySpecs::likeResultId),
            notBlank(criteria.getResultTitle()).map(ResultLegacySpecs::likeResultTitle),
            notBlank(criteria.getResultTitleCy()).map(ResultLegacySpecs::likeResultTitleCy),
            notBlank(criteria.getResultType()).map(ResultLegacySpecs::likeResultType),
            numericShort(criteria.getImpositionAllocationPriority())
                .map(ResultLegacySpecs::equalsImpositionAllocationPriority),
            notBlank(criteria.getImpositionCreditor()).map(ResultLegacySpecs::likeImpositionCreditor)
        ));
    }

    public Specification<ResultLegacyEntity> referenceDataFilter(Optional<String> filter) {
        return Specification.allOf(specificationList(
            filter.filter(s -> !s.isBlank()).map(this::likeAnyResult)
        ));
    }

    public Specification<ResultLegacyEntity> referenceDataByIds(
        Optional<List<String>> resultIds,
        Boolean active,
        Boolean manualEnforcement,
        Boolean generatesHearing,
        Boolean enforcement,
        Boolean enforcementOverride) {

        return Specification.allOf(specificationList(
            // optional resultIds clause
            resultIds.map(ResultLegacySpecs::equalsAnyResultId),

            Optional.ofNullable(active).map(ResultLegacySpecs::hasActive),
            Optional.ofNullable(manualEnforcement).map(ResultLegacySpecs::hasManualEnforcementOnly),
            Optional.ofNullable(generatesHearing).map(ResultLegacySpecs::hasGeneratesHearing),
            Optional.ofNullable(enforcement).map(ResultLegacySpecs::hasEnforcement),
            Optional.ofNullable(enforcementOverride).map(ResultLegacySpecs::hasEnforcementOverride)

        ));
    }

    public static Specification<ResultLegacyEntity> equalsAnyResultId(List<String> resultIds) {
        return (root, query, builder) -> root.get(ResultLegacyEntity_.resultId).in(resultIds);
    }

    public static Specification<ResultLegacyEntity> likeResultId(String resultId) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(ResultLegacyEntity_.resultId), builder, resultId);
    }

    public static Specification<ResultLegacyEntity> likeResultTitle(String resultTitle) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultLegacyEntity_.resultTitle), builder,
                                                               resultTitle);
    }

    public static Specification<ResultLegacyEntity> likeResultTitleCy(String resultTitleCy) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultLegacyEntity_.resultTitleCy), builder,
                                                               resultTitleCy);
    }

    public static Specification<ResultLegacyEntity> likeResultType(String resultType) {
        return (root, query, builder) -> likeWildcardPredicate(
            ((JpaExpression<?>) root.get(ResultLegacyEntity_.resultType)).cast(String.class), builder, resultType);
    }

    public static Specification<ResultLegacyEntity>
        equalsImpositionAllocationPriority(Short impositionAllocationPriority) {
        return (root, query, builder) -> builder.equal(root.get(ResultLegacyEntity_.impositionAllocationPriority),
                                                       impositionAllocationPriority);
    }

    public static Specification<ResultLegacyEntity> likeImpositionCreditor(String impositionCreditor) {
        return (root, query, builder) -> likeWildcardPredicate(
            ((JpaExpression<?>) root.get(ResultLegacyEntity_.impositionCreditor)).cast(String.class), builder,
            impositionCreditor);
    }

    public Specification<ResultLegacyEntity> likeAnyResult(String filter) {
        return Specification.anyOf(
            likeResultId(filter),
            likeResultTitle(filter),
            likeResultTitleCy(filter)
        );
    }

    public static Specification<ResultLegacyEntity> hasActive(boolean active) {
        return (root, query, builder) -> builder.equal(root.get(
            ResultLegacyEntity_.active), active);
    }

    public static Specification<ResultLegacyEntity> hasManualEnforcementOnly(boolean manualEnforcement) {
        return (root, query, builder) -> builder.equal(root.get(
            ResultLegacyEntity_.manualEnforcement), manualEnforcement);
    }

    public static Specification<ResultLegacyEntity> hasGeneratesHearing(boolean generatesHearing) {
        return (root, query, builder) -> builder.equal(root.get(
            ResultLegacyEntity_.generatesHearing), generatesHearing);
    }

    public static Specification<ResultLegacyEntity> hasEnforcement(boolean hasEnforcement) {
        return (root, query, builder) -> builder.equal(root.get(
            ResultLegacyEntity_.enforcement), hasEnforcement);
    }

    public static Specification<ResultLegacyEntity> hasEnforcementOverride(boolean enforcementOverride) {
        return (root, query, builder) -> builder.equal(root.get(ResultLegacyEntity_.enforcementOverride),
            enforcementOverride);
    }
}
