package uk.gov.hmcts.opal.repository.jpa;

import uk.gov.hmcts.opal.entity.DefendantTransactionEntity;
import uk.gov.hmcts.opal.entity.DefendantTransactionEntity_;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class DefendantTransactionSpecs extends EntitySpecs<DefendantTransactionEntity> {

    public static Specification<DefendantTransactionEntity> equalsDefendantAccountId(Long defendantId) {
        return (root, query, builder) -> equalsDefendantAccountIdPredicate(root, builder, defendantId);
    }

    public static Predicate equalsDefendantAccountIdPredicate(From<?, DefendantTransactionEntity> from,
                                                              CriteriaBuilder builder, Long defendantAccountId) {
        return builder.equal(from.get(DefendantTransactionEntity_.defendantAccountId), defendantAccountId);
    }

}
