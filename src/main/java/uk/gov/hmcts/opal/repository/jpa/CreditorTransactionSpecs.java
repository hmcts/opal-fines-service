package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.DeleteSpecification;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.entity.CreditorTransactionEntity;
import uk.gov.hmcts.opal.entity.CreditorTransactionEntity_;

public class CreditorTransactionSpecs extends EntitySpecs<CreditorTransactionEntity> {

    public static DeleteSpecification<CreditorTransactionEntity> equalsCreditorAccountIdDelete(Long creditorAccountId) {
        return (root, query, builder) ->
            builder.equal(root.get(CreditorTransactionEntity_.creditorAccountId), creditorAccountId);
    }
    public static Specification<CreditorTransactionEntity> equalsCreditorAccountIdSearch(Long creditorAccountId) {
        return (root, query, builder) ->
            builder.equal(root.get(CreditorTransactionEntity_.creditorAccountId), creditorAccountId);
    }

}
