package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;
import uk.gov.hmcts.opal.entity.ResultEntity;
import uk.gov.hmcts.opal.entity.ResultEntity_;

import java.util.Optional;

public class ResultSpecs extends EntitySpecs<ResultEntity> {

    public Specification<ResultEntity> findBySearchCriteria(ResultSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getResultId()).map(ResultSpecs::likeResultId),
            notBlank(criteria.getResultTitle()).map(ResultSpecs::likeResultTitle),
            notBlank(criteria.getResultTitleCy()).map(ResultSpecs::likeResultTitleCy),
            notBlank(criteria.getResultType()).map(ResultSpecs::likeResultType),
            notBlank(criteria.getImpositionCategory()).map(ResultSpecs::likeImpositionCategory),
            numericShort(criteria.getImpositionAllocationPriority())
                .map(ResultSpecs::equalsImpositionAllocationPriority),
            notBlank(criteria.getImpositionCreditor()).map(ResultSpecs::likeImpositionCreditor),
            notBlank(criteria.getUserEntries()).map(ResultSpecs::likeUserEntries)
        ));
    }

    public Specification<ResultEntity> referenceDataFilter(Optional<String> filter) {
        return Specification.allOf(specificationList(
            filter.filter(s -> !s.isBlank()).map(this::likeAnyResult)
        ));
    }

    public static Specification<ResultEntity> equalsResultId(String resultId) {
        return (root, query, builder) -> builder.equal(root.get(ResultEntity_.resultId), resultId);
    }

    public static Specification<ResultEntity> likeResultId(String resultId) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultEntity_.resultId), builder, resultId);
    }

    public static Specification<ResultEntity> likeResultTitle(String resultTitle) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultEntity_.resultTitle), builder,
                                                               resultTitle);
    }

    public static Specification<ResultEntity> likeResultTitleCy(String resultTitleCy) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultEntity_.resultTitleCy), builder,
                                                               resultTitleCy);
    }

    public static Specification<ResultEntity> equalsResultType(String resultType) {
        return (root, query, builder) -> builder.equal(root.get(ResultEntity_.resultType), resultType);
    }

    public static Specification<ResultEntity> likeResultType(String resultType) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultEntity_.resultType), builder, resultType);
    }

    public static Specification<ResultEntity> equalsImpositionCategory(String impositionCategory) {
        return (root, query, builder) -> builder.equal(root.get(ResultEntity_.impositionCategory), impositionCategory);
    }

    public static Specification<ResultEntity> likeImpositionCategory(String impositionCategory) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultEntity_.impositionCategory), builder,
                                                               impositionCategory);
    }

    public static Specification<ResultEntity> equalsImpositionAllocationPriority(Short impositionAllocationPriority) {
        return (root, query, builder) -> builder.equal(root.get(ResultEntity_.impositionAllocationPriority),
                                                       impositionAllocationPriority);
    }

    public static Specification<ResultEntity> equalsImpositionCreditor(String impositionCreditor) {
        return (root, query, builder) -> builder.equal(root.get(ResultEntity_.impositionCreditor), impositionCreditor);
    }

    public static Specification<ResultEntity> likeImpositionCreditor(String impositionCreditor) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultEntity_.impositionCreditor), builder,
                                                               impositionCreditor);
    }

    public static Specification<ResultEntity> equalsUserEntries(String userEntries) {
        return (root, query, builder) -> builder.equal(root.get(ResultEntity_.userEntries), userEntries);
    }

    public static Specification<ResultEntity> likeUserEntries(String userEntries) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ResultEntity_.userEntries), builder,
                                                               userEntries);
    }

    public Specification<ResultEntity> likeAnyResult(String filter) {
        return Specification.anyOf(
            likeResultId(filter),
            likeResultTitle(filter),
            likeResultTitleCy(filter)
        );
    }
}
