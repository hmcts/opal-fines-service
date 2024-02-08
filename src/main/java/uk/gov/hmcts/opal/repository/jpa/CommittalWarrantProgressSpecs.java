package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.CommittalWarrantProgressSearchDto;
import uk.gov.hmcts.opal.entity.CommittalWarrantProgressEntity;
import uk.gov.hmcts.opal.entity.CommittalWarrantProgressEntity_;

public class CommittalWarrantProgressSpecs extends EntitySpecs<CommittalWarrantProgressEntity> {

    public Specification<CommittalWarrantProgressEntity> findBySearchCriteria(
        CommittalWarrantProgressSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getDefendantAccountId())
                .map(CommittalWarrantProgressSpecs::equalsCommittalWarrantProgressId)
        ));
    }

    public static Specification<CommittalWarrantProgressEntity> equalsCommittalWarrantProgressId(
        String committalWarrantProgressId) {
        return (root, query, builder) -> builder.equal(root.get(CommittalWarrantProgressEntity_.defendantAccountId),
                                                   committalWarrantProgressId);
    }

}
