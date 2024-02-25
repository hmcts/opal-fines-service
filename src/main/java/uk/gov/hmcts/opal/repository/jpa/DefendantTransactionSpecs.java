package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.DefendantTransactionSearchDto;
import uk.gov.hmcts.opal.entity.DefendantTransactionEntity;
import uk.gov.hmcts.opal.entity.DefendantTransactionEntity_;

public class DefendantTransactionSpecs extends EntitySpecs<DefendantTransactionEntity> {

    public Specification<DefendantTransactionEntity> findBySearchCriteria(DefendantTransactionSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getDefendantTransactionId()).map(DefendantTransactionSpecs::equalsDefendantTransactionId),
            notBlank(criteria.getTransactionType()).map(DefendantTransactionSpecs::likeTransactionType),
            notBlank(criteria.getPaymentReference()).map(DefendantTransactionSpecs::likePaymentReference),
            notBlank(criteria.getText()).map(DefendantTransactionSpecs::likeText),
            notBlank(criteria.getWriteOffCode()).map(DefendantTransactionSpecs::likeWriteOffCode)
        ));
    }

    public static Specification<DefendantTransactionEntity> equalsDefendantTransactionId(
        String defendantTransactionId) {
        return (root, query, builder) -> builder.equal(root.get(DefendantTransactionEntity_.defendantTransactionId),
                                                       defendantTransactionId);
    }

    public static Specification<DefendantTransactionEntity> likeTransactionType(String transactionType) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(DefendantTransactionEntity_.transactionType), builder, transactionType);
    }

    public static Specification<DefendantTransactionEntity> likePaymentReference(String paymentReference) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(DefendantTransactionEntity_.paymentReference), builder, paymentReference);
    }

    public static Specification<DefendantTransactionEntity> likeText(String text) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(DefendantTransactionEntity_.text), builder, text);
    }

    public static Specification<DefendantTransactionEntity> likeWriteOffCode(String writeOffCode) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(DefendantTransactionEntity_.writeOffCode), builder, writeOffCode);
    }
}
