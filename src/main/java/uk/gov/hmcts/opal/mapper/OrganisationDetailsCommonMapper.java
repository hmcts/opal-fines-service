package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommon;

@Mapper(componentModel = "spring")
public interface OrganisationDetailsCommonMapper {

    @Mapping(target = "organisationAliases", ignore = true)
    OrganisationDetailsCommon toOrganisationDetailsCommon(PartyEntity party);

    @Named("toOrganisationDetailsWhenPartyIsOrganisation")
    default OrganisationDetailsCommon toOrganisationDetailsWhenPartyIsOrganisation(PartyEntity party) {
        if (party == null || !party.isOrganisation()) {
            return null;
        }

        return toOrganisationDetailsCommon(party);
    }
}
