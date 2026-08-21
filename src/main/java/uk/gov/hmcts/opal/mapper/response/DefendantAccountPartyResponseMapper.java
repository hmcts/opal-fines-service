package uk.gov.hmcts.opal.mapper.response;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.dto.common.ContactDetails;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.common.EmployerDetails;
import uk.gov.hmcts.opal.dto.common.IndividualAlias;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.common.LanguagePreference;
import uk.gov.hmcts.opal.dto.common.LanguagePreferences;
import uk.gov.hmcts.opal.dto.common.OrganisationAlias;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.VehicleDetails;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.GetPartyResponseDefendantAccount;
import uk.gov.hmcts.opal.generated.model.IndividualAliasCommonStrict;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.LanguagePreferenceCommonStrict;
import uk.gov.hmcts.opal.generated.model.LanguagePreferencesCommonStrict;
import uk.gov.hmcts.opal.generated.model.OrganisationAliasCommon;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.PartyContactDetailsDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PartyDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.PartyEmployerDetailsDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PartyVehicleDetailsDefendantAccount;

@Mapper(componentModel = "spring")
public interface DefendantAccountPartyResponseMapper {

    GetPartyResponseDefendantAccount toGeneratedResponse(GetDefendantAccountPartyResponse response);

    @Mapping(target = "defendantAccountPartyType", source = "defendantAccountPartyType",
        qualifiedByName = "toDefendantAccountPartyType")
    PartyDefendantAccount toGeneratedResponse(DefendantAccountParty party);

    PartyDetailsCommonStrict toGeneratedResponse(PartyDetails partyDetails);

    AddressDetailsCommonStrict toGeneratedResponse(AddressDetails address);

    PartyContactDetailsDefendantAccount toGeneratedResponse(ContactDetails contactDetails);

    PartyVehicleDetailsDefendantAccount toGeneratedResponse(VehicleDetails vehicleDetails);

    PartyEmployerDetailsDefendantAccount toGeneratedResponse(EmployerDetails employerDetails);

    LanguagePreferencesCommonStrict toGeneratedResponse(LanguagePreferences languagePreferences);

    default LanguagePreferenceCommonStrict toGeneratedResponse(LanguagePreference languagePreference) {
        if (languagePreference == null) {
            return null;
        }

        return LanguagePreferenceCommonStrict.builder()
            .languageCode(languagePreference.getLanguageCode() == null ? null
                : LanguagePreferenceCommonStrict.LanguageCodeEnum.fromValue(languagePreference.getLanguageCode()))
            .languageDisplayName(languagePreference.getLanguageDisplayName() == null ? null
                : LanguagePreferenceCommonStrict.LanguageDisplayNameEnum.fromValue(
                    languagePreference.getLanguageDisplayName()))
            .build();
    }

    IndividualDetailsCommonStrict toGeneratedResponse(IndividualDetails individualDetails);

    OrganisationDetailsCommonStrict toGeneratedResponse(OrganisationDetails organisationDetails);

    IndividualAliasCommonStrict toGeneratedResponse(IndividualAlias individualAlias);

    OrganisationAliasCommon toGeneratedResponse(OrganisationAlias organisationAlias);

    default JsonNullable<PartyContactDetailsDefendantAccount> toJsonNullable(ContactDetails contactDetails) {
        return JsonNullable.of(toGeneratedResponse(contactDetails));
    }

    default JsonNullable<PartyVehicleDetailsDefendantAccount> toJsonNullable(VehicleDetails vehicleDetails) {
        return JsonNullable.of(toGeneratedResponse(vehicleDetails));
    }

    default JsonNullable<PartyEmployerDetailsDefendantAccount> toJsonNullable(EmployerDetails employerDetails) {
        return JsonNullable.of(toGeneratedResponse(employerDetails));
    }

    default JsonNullable<LanguagePreferencesCommonStrict> toJsonNullable(LanguagePreferences languagePreferences) {
        return JsonNullable.of(toGeneratedResponse(languagePreferences));
    }

    default JsonNullable<OrganisationDetailsCommonStrict> toJsonNullable(OrganisationDetails organisationDetails) {
        return JsonNullable.of(toGeneratedResponse(organisationDetails));
    }

    default JsonNullable<IndividualDetailsCommonStrict> toJsonNullable(IndividualDetails individualDetails) {
        return JsonNullable.of(toGeneratedResponse(individualDetails));
    }

    default JsonNullable<LanguagePreferenceCommonStrict> toJsonNullable(LanguagePreference languagePreference) {
        return JsonNullable.of(toGeneratedResponse(languagePreference));
    }

    default <T> JsonNullable<T> toJsonNullable(T value) {
        return JsonNullable.of(value);
    }

    default JsonNullable<List<OrganisationAliasCommon>> toNullableOrganisationAliases(
        List<OrganisationAlias> organisationAliases) {
        return JsonNullable.of(organisationAliases == null ? null
            : organisationAliases.stream().map(this::toGeneratedResponse).toList());
    }

    default JsonNullable<List<IndividualAliasCommonStrict>> toNullableIndividualAliases(
        List<IndividualAlias> individualAliases) {
        return JsonNullable.of(individualAliases == null ? null
            : individualAliases.stream().map(this::toGeneratedResponse).toList());
    }

    @Named("toDefendantAccountPartyType")
    default PartyDefendantAccount.DefendantAccountPartyTypeEnum toDefendantAccountPartyType(String partyType) {
        return partyType == null ? null : PartyDefendantAccount.DefendantAccountPartyTypeEnum.fromValue(partyType);
    }
}
