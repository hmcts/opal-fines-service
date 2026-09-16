package uk.gov.hmcts.opal.mapper.request;

import java.util.List;
import org.mapstruct.Mapper;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.DefendantAccountParty;
import uk.gov.hmcts.opal.generated.model.IndividualAliasCommon;
import uk.gov.hmcts.opal.generated.model.IndividualAliasCommonStrict;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommon;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommon;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.ReplacePartyRequestDefendantAccount;

@Mapper(componentModel = "spring")
public interface ReplacePartyRequestMapper {

    DefendantAccountParty toDefendantAccountParty(ReplacePartyRequestDefendantAccount request);

    PartyDetailsCommonStrict toPartyDetails(PartyDetailsCommon partyDetails);

    AddressDetailsCommonStrict toAddress(AddressDetailsCommon address);

    OrganisationDetailsCommonStrict toOrganisationDetails(OrganisationDetailsCommon organisationDetails);

    IndividualDetailsCommonStrict toIndividualDetails(IndividualDetailsCommon individualDetails);

    IndividualAliasCommonStrict toIndividualAlias(IndividualAliasCommon individualAlias);

    List<IndividualAliasCommonStrict> toIndividualAliases(List<IndividualAliasCommon> individualAliases);

    default JsonNullable<OrganisationDetailsCommonStrict> toJsonNullable(
        OrganisationDetailsCommon organisationDetails) {
        return JsonNullable.of(toOrganisationDetails(organisationDetails));
    }

    default JsonNullable<IndividualDetailsCommonStrict> toJsonNullable(IndividualDetailsCommon individualDetails) {
        return JsonNullable.of(toIndividualDetails(individualDetails));
    }

    default JsonNullable<List<IndividualAliasCommonStrict>> toJsonNullable(
        List<IndividualAliasCommon> individualAliases) {
        return JsonNullable.of(toIndividualAliases(individualAliases));
    }

    default <T> JsonNullable<T> toJsonNullable(T value) {
        return JsonNullable.of(value);
    }
}
