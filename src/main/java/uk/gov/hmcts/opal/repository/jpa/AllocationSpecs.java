package uk.gov.hmcts.opal.repository.jpa;


import uk.gov.hmcts.opal.entity.AllocationEntity;
import uk.gov.hmcts.opal.entity.AllocationEntity_;
import uk.gov.hmcts.opal.entity.DefendantTransactionEntity;
import uk.gov.hmcts.opal.entity.imposition.ImpositionFullEntity;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class AllocationSpecs extends EntitySpecs<AllocationEntity> {

    public static Specification<AllocationEntity> equalsDefendantTransactionAccountId(Long defendantAccountId) {
        return (root, query, builder) -> DefendantTransactionSpecs.equalsDefendantAccountIdPredicate(
            joinDefendantTransaction(root), builder, defendantAccountId);
    }

    public static Specification<AllocationEntity> equalsImpositionDefendantAccountId(Long defendantAccountId) {
        return (root, query, builder) -> ImpositionFullSpecs.equalsDefendantAccountIdPredicate(
            joinImposition(root), builder, defendantAccountId);
    }

    public static Join<AllocationEntity, DefendantTransactionEntity> joinDefendantTransaction(
        Root<AllocationEntity> root) {
        return root.join(AllocationEntity_.defendantTransaction);
    }

    public static Join<AllocationEntity, ImpositionFullEntity> joinImposition(
        Root<AllocationEntity> root) {
        return root.join(AllocationEntity_.imposition);
    }

}
