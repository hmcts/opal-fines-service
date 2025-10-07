package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.entity.result.ResultEntity_;
import uk.gov.hmcts.opal.entity.result.ResultEntity.Lite;

import java.util.List;
import java.util.Optional;

public class ResultSpecsLite extends EntitySpecs<Lite> {

    public Specification<Lite> findBySearchCriteria(ResultSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getResultId()).map(ResultSpecsLite::likeResultId),
            notBlank(criteria.getResultTitle()).map(ResultSpecsLite::likeResultTitle),
            notBlank(criteria.getResultTitleCy()).map(ResultSpecsLite::likeResultTitleCy),
            notBlank(criteria.getResultType()).map(ResultSpecsLite::likeResultType),
            numericShort(criteria.getImpositionAllocationPriority())
                .map(ResultSpecsLite::equalsImpositionAllocationPriority),
            notBlank(criteria.getImpositionCreditor()).map(ResultSpecsLite::likeImpositionCreditor)
        ));
    }

    public Specification<Lite> referenceDataFilter(Optional<String> filter) {
        return Specification.allOf(specificationList(
            filter.filter(s -> !s.isBlank()).map(this::likeAnyResult)
        ));
    }

    public Specification<ResultEntity.Lite> referenceDataByIds(Optional<List<String>> resultIds) {
        return Specification.allOf(specificationList(
            resultIds.map(ResultSpecsLite::equalsAnyResultId)
        ));
    }

    public static Specification<ResultEntity.Lite> equalsAnyResultId(List<String> resultIds) {
        return (root, query, builder) -> root.get(ResultEntity_.resultId).in(resultIds);
    }

    public static Specification<Lite> likeResultId(String resultId) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(ResultEntity_.resultId), builder, resultId);
    }

    public static Specification<Lite> likeResultTitle(String resultTitle) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultEntity_.resultTitle), builder,
                                                               resultTitle);
    }

    public static Specification<ResultEntity.Lite> likeResultTitleCy(String resultTitleCy) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultEntity_.resultTitleCy), builder,
                                                               resultTitleCy);
    }

    public static Specification<Lite> likeResultType(String resultType) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(ResultEntity_.resultType), builder, resultType);
    }

    public static Specification<Lite>
        equalsImpositionAllocationPriority(Short impositionAllocationPriority) {
        return (root, query, builder) -> builder.equal(root.get(ResultEntity_.impositionAllocationPriority),
                                                       impositionAllocationPriority);
    }

    public static Specification<ResultEntity.Lite> likeImpositionCreditor(String impositionCreditor) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultEntity_.impositionCreditor), builder,
                                                               impositionCreditor);
    }

    public Specification<Lite> likeAnyResult(String filter) {
        return Specification.anyOf(
            likeResultId(filter),
            likeResultTitle(filter),
            likeResultTitleCy(filter)
        );
    }
}
