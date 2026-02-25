package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.DeleteSpecification;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity.Lite;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity_;

public class ImpositionSpecs extends EntitySpecs<ImpositionEntity.Lite> {

    public static DeleteSpecification<Lite> equalsCreditorAccountIdDeletionSpec(Long creditorAccountId) {
        return (root, query, builder) ->
            builder.equal(root.get(ImpositionEntity_.creditorAccountId), creditorAccountId);
    }
    public static Specification<Lite> equalsCreditorAccountIdSearch(Long creditorAccountId) {
        return (root, query, builder) ->
            builder.equal(root.get(ImpositionEntity_.creditorAccountId), creditorAccountId);
    }
}
