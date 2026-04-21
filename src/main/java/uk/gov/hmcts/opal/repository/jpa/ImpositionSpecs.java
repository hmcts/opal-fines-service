package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.DeleteSpecification;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity_;

public class ImpositionSpecs extends EntitySpecs<ImpositionEntity> {

    public static DeleteSpecification<ImpositionEntity> equalsCreditorAccountIdDeletionSpec(Long creditorAccountId) {
        return (root, query, builder) ->
            builder.equal(root.get(ImpositionEntity_.creditorAccountId), creditorAccountId);
    }

    public static Specification<ImpositionEntity> equalsCreditorAccountId(Long creditorAccountId) {
        return equalsCreditorAccountIdSearch(creditorAccountId);
    }

    public static Specification<ImpositionEntity> equalsCreditorAccountIdSearch(Long creditorAccountId) {
        return (root, query, builder) ->
            builder.equal(root.get(ImpositionEntity_.creditorAccountId), creditorAccountId);
    }

    public static Predicate equalsDefendantAccountIdPredicate(From<?, ImpositionEntity> from,
                                                              CriteriaBuilder builder, Long defendantAccountId) {
        return builder.equal(from.get(ImpositionEntity_.defendantAccountId), defendantAccountId);
    }

}
