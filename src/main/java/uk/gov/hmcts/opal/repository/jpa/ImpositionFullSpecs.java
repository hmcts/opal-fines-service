package uk.gov.hmcts.opal.repository.jpa;

import uk.gov.hmcts.opal.entity.imposition.ImpositionFullEntity;
import uk.gov.hmcts.opal.entity.imposition.ImpositionFullEntity_;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;

public class ImpositionFullSpecs extends EntitySpecs<ImpositionFullEntity> {

    public static Predicate equalsDefendantAccountIdPredicate(From<?, ImpositionFullEntity> from,
                                                              CriteriaBuilder builder, Long defendantAccountId) {
        return builder.equal(from.get(ImpositionFullEntity_.defendantAccountId), defendantAccountId);
    }
}
