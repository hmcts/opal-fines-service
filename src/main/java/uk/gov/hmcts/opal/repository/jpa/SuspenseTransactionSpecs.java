package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.SuspenseTransactionSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseTransactionEntity;
import uk.gov.hmcts.opal.entity.SuspenseTransactionEntity_;

public class SuspenseTransactionSpecs extends EntitySpecs<SuspenseTransactionEntity> {

    public Specification<SuspenseTransactionEntity> findBySearchCriteria(SuspenseTransactionSearchDto criteria) {
        return Specification.allOf(specificationList(
            numericLong(criteria.getSuspenseTransactionId()).map(SuspenseTransactionSpecs::equalsSuspenseTransactionId),
            numericLong(criteria.getSuspenseItemId()).map(SuspenseTransactionSpecs::equalsSuspenseItemId),
            notBlank(criteria.getPostedBy()).map(SuspenseTransactionSpecs::likePostedBy),
            numericLong(criteria.getPostedByUserId()).map(SuspenseTransactionSpecs::equalsPostedByUserId),
            notBlank(criteria.getTransactionType()).map(SuspenseTransactionSpecs::likeTransactionType),
            notBlank(criteria.getAssociatedRecordType()).map(SuspenseTransactionSpecs::likeAssociatedRecordType),
            notBlank(criteria.getAssociatedRecordId()).map(SuspenseTransactionSpecs::likeAssociatedRecordId),
            notBlank(criteria.getText()).map(SuspenseTransactionSpecs::likeText),
            notBlank(criteria.getReversed()).map(SuspenseTransactionSpecs::likeReversed)
        ));
    }

    public static Specification<SuspenseTransactionEntity> equalsSuspenseTransactionId(Long suspenseTransactionId) {
        return (root, query, builder) -> builder.equal(root.get(SuspenseTransactionEntity_.suspenseTransactionId),
                                                       suspenseTransactionId);
    }

    public static Specification<SuspenseTransactionEntity> equalsSuspenseItemId(Long suspenseItemId) {
        return (root, query, builder) -> builder.equal(root.get(SuspenseTransactionEntity_.suspenseItemId),
                                                       suspenseItemId);
    }

    public static Specification<SuspenseTransactionEntity> likePostedBy(String postedBy) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(SuspenseTransactionEntity_.postedBy), builder, postedBy);
    }

    public static Specification<SuspenseTransactionEntity> equalsPostedByUserId(Long postedByUserId) {
        return (root, query, builder) -> builder.equal(root.get(SuspenseTransactionEntity_.postedByUserId),
                                                       postedByUserId);
    }

    public static Specification<SuspenseTransactionEntity> likeTransactionType(String transactionType) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(SuspenseTransactionEntity_.transactionType), builder, transactionType);
    }

    public static Specification<SuspenseTransactionEntity> likeAssociatedRecordType(String associatedRecordType) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(SuspenseTransactionEntity_.associatedRecordType), builder,
                                  associatedRecordType);
    }

    public static Specification<SuspenseTransactionEntity> likeAssociatedRecordId(String associatedRecordId) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(SuspenseTransactionEntity_.associatedRecordId), builder, associatedRecordId);
    }

    public static Specification<SuspenseTransactionEntity> likeText(String text) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(SuspenseTransactionEntity_.text), builder, text);
    }

    public static Specification<SuspenseTransactionEntity> likeReversed(String reversed) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(SuspenseTransactionEntity_.reversed), builder, reversed);
    }

}
