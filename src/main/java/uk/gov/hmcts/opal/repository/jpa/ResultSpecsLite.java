package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;
import uk.gov.hmcts.opal.entity.result.ResultEntityLite;
import uk.gov.hmcts.opal.entity.result.ResultEntityLite_;

import java.util.List;
import java.util.Optional;

public class ResultSpecsLite extends EntitySpecs<ResultEntityLite> {

    public Specification<ResultEntityLite> findBySearchCriteria(ResultSearchDto criteria) {
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

    public Specification<ResultEntityLite> referenceDataFilter(Optional<String> filter) {
        return Specification.allOf(specificationList(
            filter.filter(s -> !s.isBlank()).map(this::likeAnyResult)
        ));
    }

    public Specification<ResultEntityLite> referenceDataByIds(Optional<List<String>> resultIds) {
        return Specification.allOf(specificationList(
            resultIds.map(ResultSpecsLite::equalsAnyResultId)
        ));
    }

    public static Specification<ResultEntityLite> equalsAnyResultId(List<String> resultIds) {
        return (root, query, builder) -> root.get(ResultEntityLite_.resultId).in(resultIds);
    }

    public static Specification<ResultEntityLite> likeResultId(String resultId) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultEntityLite_.resultId), builder, resultId);
    }

    public static Specification<ResultEntityLite> likeResultTitle(String resultTitle) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultEntityLite_.resultTitle), builder,
                                                               resultTitle);
    }

    public static Specification<ResultEntityLite> likeResultTitleCy(String resultTitleCy) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultEntityLite_.resultTitleCy), builder,
                                                               resultTitleCy);
    }

    public static Specification<ResultEntityLite> likeResultType(String resultType) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(ResultEntityLite_.resultType), builder, resultType);
    }

    public static Specification<ResultEntityLite>
        equalsImpositionAllocationPriority(Short impositionAllocationPriority) {
        return (root, query, builder) -> builder.equal(root.get(ResultEntityLite_.impositionAllocationPriority),
                                                       impositionAllocationPriority);
    }

    public static Specification<ResultEntityLite> likeImpositionCreditor(String impositionCreditor) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultEntityLite_.impositionCreditor), builder,
                                                               impositionCreditor);
    }

    public Specification<ResultEntityLite> likeAnyResult(String filter) {
        return Specification.anyOf(
            likeResultId(filter),
            likeResultTitle(filter),
            likeResultTitleCy(filter)
        );
    }
}
