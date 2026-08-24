package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.dto.legacy.AddDefendantAccountPartyLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.ContactDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountPartyLegacy;
import uk.gov.hmcts.opal.dto.legacy.EmployerDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.IndividualDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.LanguagePreferencesLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyReplaceDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.legacy.OrganisationDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.VehicleDetailsLegacy;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.LanguagePreferenceCommonStrict;
import uk.gov.hmcts.opal.generated.model.LanguagePreferencesCommonStrict;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.PartyContactDetailsDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountParty;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.PartyEmployerDetailsDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PartyResponseDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PartyVehicleDetailsDefendantAccount;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DefendantAccountPartyLegacyResponseMapper {

    PartyResponseDefendantAccount toGeneratedResponse(AddDefendantAccountPartyLegacyResponse legacy);

    PartyResponseDefendantAccount toGeneratedResponse(LegacyReplaceDefendantAccountPartyResponse legacy);

    PartyResponseDefendantAccount toGeneratedResponse(GetDefendantAccountPartyLegacyResponse legacy);

    @Mapping(target = "defendantAccountPartyType", source = "defendantAccountPartyType",
        qualifiedByName = "toDefendantAccountPartyType")
    DefendantAccountParty toGeneratedResponse(DefendantAccountPartyLegacy legacy);

    PartyDetailsCommonStrict toGeneratedResponse(PartyDetailsLegacy legacy);

    AddressDetailsCommonStrict toGeneratedResponse(AddressDetailsLegacy legacy);

    PartyContactDetailsDefendantAccount toGeneratedResponse(ContactDetailsLegacy legacy);

    PartyVehicleDetailsDefendantAccount toGeneratedResponse(VehicleDetailsLegacy legacy);

    @Mapping(target = "employerAddress", source = "employerAddress", qualifiedByName = "toEmployerAddress")
    PartyEmployerDetailsDefendantAccount toGeneratedResponse(EmployerDetailsLegacy legacy);

    OrganisationDetailsCommonStrict toGeneratedResponse(OrganisationDetailsLegacy legacy);

    IndividualDetailsCommonStrict toGeneratedResponse(IndividualDetailsLegacy legacy);

    LanguagePreferencesCommonStrict toGeneratedResponse(LanguagePreferencesLegacy legacy);

    @Mapping(target = "languageCode", source = "languageCode", qualifiedByName = "toLanguageCode")
    @Mapping(target = "languageDisplayName", source = "languageCode", qualifiedByName = "toLanguageDisplayName")
    LanguagePreferenceCommonStrict toGeneratedResponse(
        LanguagePreferencesLegacy.LanguagePreference legacyPreference);

    default <T> JsonNullable<T> toJsonNullable(T value) {
        return JsonNullable.of(value);
    }

    default JsonNullable<PartyContactDetailsDefendantAccount> toJsonNullable(ContactDetailsLegacy legacy) {
        boolean empty = legacy == null || legacy.getPrimaryEmailAddress() == null
            && legacy.getSecondaryEmailAddress() == null
            && legacy.getMobileTelephoneNumber() == null
            && legacy.getHomeTelephoneNumber() == null
            && legacy.getWorkTelephoneNumber() == null;
        return JsonNullable.of(empty ? null : toGeneratedResponse(legacy));
    }

    default JsonNullable<PartyVehicleDetailsDefendantAccount> toJsonNullable(VehicleDetailsLegacy legacy) {
        boolean empty = legacy == null || legacy.getVehicleMakeAndModel() == null
            && legacy.getVehicleRegistration() == null;
        return JsonNullable.of(empty ? null : toGeneratedResponse(legacy));
    }

    default JsonNullable<PartyEmployerDetailsDefendantAccount> toJsonNullable(EmployerDetailsLegacy legacy) {
        boolean empty = legacy == null || legacy.getEmployerName() == null
            && legacy.getEmployerReference() == null
            && legacy.getEmployerEmailAddress() == null
            && legacy.getEmployerTelephoneNumber() == null
            && legacy.getEmployerAddress() == null;
        return JsonNullable.of(empty ? null : toGeneratedResponse(legacy));
    }

    default JsonNullable<LanguagePreferencesCommonStrict> toJsonNullable(LanguagePreferencesLegacy legacy) {
        return JsonNullable.of(toGeneratedResponse(legacy));
    }

    default JsonNullable<OrganisationDetailsCommonStrict> toJsonNullable(OrganisationDetailsLegacy legacy) {
        return JsonNullable.of(toGeneratedResponse(legacy));
    }

    default JsonNullable<IndividualDetailsCommonStrict> toJsonNullable(IndividualDetailsLegacy legacy) {
        return JsonNullable.of(toGeneratedResponse(legacy));
    }

    default JsonNullable<LanguagePreferenceCommonStrict> toJsonNullable(
        LanguagePreferencesLegacy.LanguagePreference legacy) {
        return JsonNullable.of(toGeneratedResponse(legacy));
    }

    @Named("toEmployerAddress")
    default AddressDetailsCommonStrict toEmployerAddress(AddressDetailsLegacy legacy) {
        return legacy == null || legacy.getAddressLine1() == null ? null : toGeneratedResponse(legacy);
    }

    @Named("toDefendantAccountPartyType")
    default DefendantAccountParty.DefendantAccountPartyTypeEnum toDefendantAccountPartyType(String partyType) {
        return partyType == null ? null : DefendantAccountParty.DefendantAccountPartyTypeEnum.fromValue(partyType);
    }

    @Named("toLanguageCode")
    default LanguagePreferenceCommonStrict.LanguageCodeEnum toLanguageCode(String languageCode) {
        return languageCode == null ? null
            : LanguagePreferenceCommonStrict.LanguageCodeEnum.fromValue(languageCode.trim().toUpperCase());
    }

    @Named("toLanguageDisplayName")
    default LanguagePreferenceCommonStrict.LanguageDisplayNameEnum toLanguageDisplayName(String languageCode) {
        return switch (toLanguageCode(languageCode)) {
            case CY -> LanguagePreferenceCommonStrict.LanguageDisplayNameEnum.WELSH_AND_ENGLISH;
            case EN -> LanguagePreferenceCommonStrict.LanguageDisplayNameEnum.ENGLISH_ONLY;
            case null -> null;
        };
    }
}
