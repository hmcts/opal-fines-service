package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.PartySearchDto;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PartyEntity_;

public class PartySpecs extends EntitySpecs<PartyEntity> {

    public Specification<PartyEntity> findBySearchCriteria(PartySearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getPartyId()).map(PartySpecs::equalsPartyId)
        ));
    }

    public static Specification<PartyEntity> equalsPartyId(String partyId) {
        return (root, query, builder) -> builder.equal(root.get(PartyEntity_.partyId), partyId);
    }

}
