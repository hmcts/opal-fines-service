package uk.gov.hmcts.opal.repository.jpa;

import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity_;

import org.springframework.data.jpa.domain.Specification;

public class ImpositionSpecs extends EntitySpecs<ImpositionEntity.Lite> {

    public static Specification<ImpositionEntity.Lite> equalsCreditorAccountId(Long creditorAccountId) {
        return (root, query, builder) ->
            builder.equal(root.get(ImpositionEntity_.creditorAccountId), creditorAccountId);
    }

}
