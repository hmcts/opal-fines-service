package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.DeleteSpecification;
import uk.gov.hmcts.opal.entity.AllocationEntity;
import uk.gov.hmcts.opal.entity.AllocationEntity_;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionEntity;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;

public class AllocationSpecs extends EntitySpecs<AllocationEntity> {

    public static DeleteSpecification<AllocationEntity> equalsDefendantTransactionAccountId(Long defendantAccountId) {
        return (root, query, builder) -> DefendantTransactionSpecs.equalsDefendantAccountIdPredicate(
            joinDefendantTransaction(root), builder, defendantAccountId);
    }

    public static DeleteSpecification<AllocationEntity> equalsImpositionDefendantAccountId(Long defendantAccountId) {
        return (root, query, builder) -> ImpositionSpecs.equalsDefendantAccountIdPredicate(
            joinImposition(root), builder, defendantAccountId);
    }

    public static Join<AllocationEntity, DefendantTransactionEntity> joinDefendantTransaction(
        Root<AllocationEntity> root) {
        return root.join(AllocationEntity_.defendantTransaction);
    }

    public static Join<AllocationEntity, ImpositionEntity> joinImposition(
        Root<AllocationEntity> root) {
        return root.join(AllocationEntity_.imposition);
    }

}
