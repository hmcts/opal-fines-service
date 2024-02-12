package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.CommittalWarrantProgressSearchDto;
import uk.gov.hmcts.opal.entity.CommittalWarrantProgressEntity;
import uk.gov.hmcts.opal.entity.CommittalWarrantProgressEntity_;

public class CommittalWarrantProgressSpecs extends EntitySpecs<CommittalWarrantProgressEntity> {

    public Specification<CommittalWarrantProgressEntity> findBySearchCriteria(
        CommittalWarrantProgressSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getDefendantAccountId()).map(CommittalWarrantProgressSpecs::equalsDefendantAccountId),
            notBlank(criteria.getEnforcementId()).map(CommittalWarrantProgressSpecs::equalsEnforcement),
            notBlank(criteria.getPrisonId()).map(CommittalWarrantProgressSpecs::equalsPrison)
        ));
    }

    public static Specification<CommittalWarrantProgressEntity> equalsDefendantAccountId(
        String defendantAccountId) {
        return (root, query, builder) -> builder.equal(root.get(CommittalWarrantProgressEntity_.defendantAccountId),
                                                       defendantAccountId);
    }

    public static Specification<CommittalWarrantProgressEntity> equalsEnforcement(
        String enforcement) {
        return (root, query, builder) -> builder.equal(root.get(CommittalWarrantProgressEntity_.enforcement),
                                                       enforcement);
    }

    public static Specification<CommittalWarrantProgressEntity> equalsPrison(
        String prison) {
        return (root, query, builder) -> builder.equal(root.get(CommittalWarrantProgressEntity_.prison),
                                                       prison);
    }

}
