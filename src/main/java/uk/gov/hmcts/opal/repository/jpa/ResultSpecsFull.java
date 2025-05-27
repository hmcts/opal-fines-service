package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;
import uk.gov.hmcts.opal.entity.result.ResultEntityFull;
import uk.gov.hmcts.opal.entity.result.ResultEntityFull_;

import java.util.Optional;

public class ResultSpecsFull extends EntitySpecs<ResultEntityFull> {

    public Specification<ResultEntityFull> findBySearchCriteria(ResultSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getResultId()).map(ResultSpecsFull::likeResultId),
            notBlank(criteria.getResultTitle()).map(ResultSpecsFull::likeResultTitle),
            notBlank(criteria.getResultTitleCy()).map(ResultSpecsFull::likeResultTitleCy),
            notBlank(criteria.getResultType()).map(ResultSpecsFull::likeResultType),
            notBlank(criteria.getImpositionCategory()).map(ResultSpecsFull::likeImpositionCategory),
            numericShort(criteria.getImpositionAllocationPriority())
                .map(ResultSpecsFull::equalsImpositionAllocationPriority),
            notBlank(criteria.getImpositionCreditor()).map(ResultSpecsFull::likeImpositionCreditor),
            notBlank(criteria.getResultParameters()).map(ResultSpecsFull::likeResultParameters)
        ));
    }

    public Specification<ResultEntityFull> referenceDataFilter(Optional<String> filter) {
        return Specification.allOf(specificationList(
            filter.filter(s -> !s.isBlank()).map(this::likeAnyResult)
        ));
    }

    public static Specification<ResultEntityFull> likeResultId(String resultId) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultEntityFull_.resultId), builder, resultId);
    }

    public static Specification<ResultEntityFull> likeResultTitle(String resultTitle) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultEntityFull_.resultTitle), builder,
                                                               resultTitle);
    }

    public static Specification<ResultEntityFull> likeResultTitleCy(String resultTitleCy) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultEntityFull_.resultTitleCy), builder,
                                                               resultTitleCy);
    }

    public static Specification<ResultEntityFull> likeResultType(String resultType) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(ResultEntityFull_.resultType), builder, resultType);
    }

    public static Specification<ResultEntityFull> likeImpositionCategory(String impositionCategory) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(ResultEntityFull_.impositionCategory), builder,
                                                               impositionCategory);
    }

    public static Specification<ResultEntityFull>
        equalsImpositionAllocationPriority(Short impositionAllocationPriority) {
        return (root, query, builder) -> builder.equal(root.get(ResultEntityFull_.impositionAllocationPriority),
                                                       impositionAllocationPriority);
    }

    public static Specification<ResultEntityFull> likeImpositionCreditor(String impositionCreditor) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultEntityFull_.impositionCreditor), builder,
                                                               impositionCreditor);
    }

    public static Specification<ResultEntityFull> likeResultParameters(String resultParameters) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultEntityFull_.resultParameters), builder,
                                                               resultParameters);
    }

    public Specification<ResultEntityFull> likeAnyResult(String filter) {
        return Specification.anyOf(
            likeResultId(filter),
            likeResultTitle(filter),
            likeResultTitleCy(filter)
        );
    }
}
