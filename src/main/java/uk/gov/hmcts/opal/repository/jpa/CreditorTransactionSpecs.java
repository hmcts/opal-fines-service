package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.DeleteSpecification;
import uk.gov.hmcts.opal.entity.CreditorTransactionEntity;
import uk.gov.hmcts.opal.entity.CreditorTransactionEntity_;

import org.springframework.data.jpa.domain.Specification;

public class CreditorTransactionSpecs extends EntitySpecs<CreditorTransactionEntity> {

    public static DeleteSpecification<CreditorTransactionEntity> equalsCreditorAccountId(Long creditorAccountId) {
        return (root, query, builder) ->
            builder.equal(root.get(CreditorTransactionEntity_.creditorAccountId), creditorAccountId);
    }

}
