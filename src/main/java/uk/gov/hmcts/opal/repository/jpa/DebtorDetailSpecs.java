package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.DebtorDetailSearchDto;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity_;

public class DebtorDetailSpecs extends EntitySpecs<DebtorDetailEntity> {

    public Specification<DebtorDetailEntity> findBySearchCriteria(DebtorDetailSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getPartyId()).map(DebtorDetailSpecs::equalsPartyId)
        ));
    }

    public static Specification<DebtorDetailEntity> equalsPartyId(String partyId) {
        return (root, query, builder) -> builder.equal(root.get(DebtorDetailEntity_.partyId), partyId);
    }

}
