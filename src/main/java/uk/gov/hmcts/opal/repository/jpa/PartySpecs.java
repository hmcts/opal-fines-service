package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.PartySearchDto;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PartyEntity_;

public class PartySpecs extends EntitySpecs<PartyEntity> {

    public Specification<PartyEntity> findBySearchCriteria(PartySearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getPartyId()).map(PartySpecs::equalsPartyId),
            notBlank(criteria.getOrganisationName()).map(PartySpecs::equalsOrganisationName),
            notBlank(criteria.getSurname()).map(PartySpecs::equalsSurname),
            notBlank(criteria.getForenames()).map(PartySpecs::equalsForenames),
            notBlank(criteria.getAddressLine()).map(PartySpecs::equalsAddressLine),
            notBlank(criteria.getPostcode()).map(PartySpecs::equalsPostcode)
        ));
    }

    public static Specification<PartyEntity> equalsPartyId(String partyId) {
        return (root, query, builder) -> builder.equal(root.get(PartyEntity_.partyId), partyId);
    }

    public static Specification<PartyEntity> equalsOrganisationName(String organisationName) {
        return (root, query, builder) -> builder.equal(root.get(PartyEntity_.organisationName), organisationName);
    }

    public static Specification<PartyEntity> equalsSurname(String surname) {
        return (root, query, builder) -> builder.equal(root.get(PartyEntity_.surname), surname);
    }

    public static Specification<PartyEntity> equalsForenames(String forenames) {
        return (root, query, builder) -> builder.equal(root.get(PartyEntity_.forenames), forenames);
    }

    public static Specification<PartyEntity> equalsAddressLine(String addressLine) {
        return (root, query, builder) -> builder.equal(root.get(PartyEntity_.addressLine1), addressLine);
    }

    public static Specification<PartyEntity> equalsPostcode(String postcode) {
        return (root, query, builder) -> builder.equal(root.get(PartyEntity_.postcode), postcode);
    }

}
