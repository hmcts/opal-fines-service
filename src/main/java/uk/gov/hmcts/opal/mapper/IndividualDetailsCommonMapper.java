package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommon;

@Mapper(componentModel = "spring")
public interface IndividualDetailsCommonMapper {

    @Mapping(target = "dateOfBirth", ignore = true)
    @Mapping(target = "nationalInsuranceNumber", ignore = true)
    @Mapping(target = "individualAliases", ignore = true)
    IndividualDetailsCommon toIndividualDetailsCommon(PartyEntity party);

    @Named("toIndividualDetailsWhenPartyIsIndividual")
    default IndividualDetailsCommon toIndividualDetailsWhenPartyIsIndividual(PartyEntity party) {
        if (party == null || party.isOrganisation()) {
            return null;
        }

        return toIndividualDetailsCommon(party);
    }
}
