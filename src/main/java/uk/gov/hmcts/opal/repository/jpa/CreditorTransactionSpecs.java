package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.CreditorTransactionSearchDto;
import uk.gov.hmcts.opal.entity.CreditorTransactionEntity;
import uk.gov.hmcts.opal.entity.CreditorTransactionEntity_;
import uk.gov.hmcts.opal.entity.UserEntity;

import static uk.gov.hmcts.opal.repository.jpa.UserSpecs.equalsUserIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.UserSpecs.likeUserDescriptionPredicate;

public class CreditorTransactionSpecs extends EntitySpecs<CreditorTransactionEntity> {

    public Specification<CreditorTransactionEntity> findBySearchCriteria(CreditorTransactionSearchDto criteria) {
        return Specification.allOf(specificationList(
            numericLong(criteria.getCreditorTransactionId()).map(CreditorTransactionSpecs::equalsCreditorTransactionId),
            numericLong(criteria.getCreditorAccountId()).map(CreditorTransactionSpecs::equalsCreditorAccountId),
            notBlank(criteria.getPostedBy()).map(CreditorTransactionSpecs::likePostedBy),
            numericLong(criteria.getPostedByUserId()).map(CreditorTransactionSpecs::equalsPostedByUserId),
            notBlank(criteria.getTransactionType()).map(CreditorTransactionSpecs::likeTransactionType),
            notBlank(criteria.getImpositionResultId()).map(CreditorTransactionSpecs::likeImpositionResultId),
            notBlank(criteria.getPaymentReference()).map(CreditorTransactionSpecs::likePaymentReference),
            notBlank(criteria.getStatus()).map(CreditorTransactionSpecs::likeStatus),
            notBlank(criteria.getAssociatedRecordType()).map(CreditorTransactionSpecs::likeAssociatedRecordType),
            notBlank(criteria.getAssociatedRecordId()).map(CreditorTransactionSpecs::likeAssociatedRecordId)
        ));
    }

    public static Specification<CreditorTransactionEntity> equalsCreditorTransactionId(Long creditorTransactionId) {
        return (root, query, builder) -> builder.equal(root.get(CreditorTransactionEntity_.creditorTransactionId),
                                                       creditorTransactionId);
    }

    public static Specification<CreditorTransactionEntity> equalsCreditorAccountId(Long creditorAccountId) {
        return (root, query, builder) -> builder.equal(root.get(CreditorTransactionEntity_.creditorAccountId),
                                                       creditorAccountId);
    }

    public static Specification<CreditorTransactionEntity> likePostedBy(String postedBy) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CreditorTransactionEntity_.postedBy), builder, postedBy);
    }

    public static Specification<CreditorTransactionEntity> equalsPostedByUserId(Long postedByUserId) {
        return (root, query, builder) -> equalsUserIdPredicate(joinPostedByUser(root), builder, postedByUserId);
    }

    public static Specification<CreditorTransactionEntity> likePostedByUserDescription(String description) {
        return (root, query, builder) -> likeUserDescriptionPredicate(joinPostedByUser(root), builder, description);
    }


    public static Specification<CreditorTransactionEntity> likeTransactionType(String transactionType) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CreditorTransactionEntity_.transactionType), builder, transactionType);
    }

    public static Specification<CreditorTransactionEntity> likeImpositionResultId(String impositionResultId) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CreditorTransactionEntity_.impositionResultId), builder, impositionResultId);
    }

    public static Specification<CreditorTransactionEntity> likePaymentReference(String paymentReference) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CreditorTransactionEntity_.paymentReference), builder, paymentReference);
    }

    public static Specification<CreditorTransactionEntity> likeStatus(String status) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CreditorTransactionEntity_.status), builder, status);
    }

    public static Specification<CreditorTransactionEntity> likeAssociatedRecordType(String associatedRecordType) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CreditorTransactionEntity_.associatedRecordType), builder,
                                  associatedRecordType);
    }

    public static Specification<CreditorTransactionEntity> likeAssociatedRecordId(String associatedRecordId) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(CreditorTransactionEntity_.associatedRecordId), builder, associatedRecordId);
    }

    public static Join<CreditorTransactionEntity, UserEntity> joinPostedByUser(Root<CreditorTransactionEntity> root) {
        return root.join(CreditorTransactionEntity_.postedByUsername);
    }
}
