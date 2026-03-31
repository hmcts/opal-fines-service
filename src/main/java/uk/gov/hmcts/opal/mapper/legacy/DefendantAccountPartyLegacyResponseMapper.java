package uk.gov.hmcts.opal.mapper.legacy;

import java.math.BigInteger;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.dto.common.ContactDetails;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.common.EmployerDetails;
import uk.gov.hmcts.opal.dto.common.LanguagePreference;
import uk.gov.hmcts.opal.dto.common.LanguagePreferences;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.VehicleDetails;
import uk.gov.hmcts.opal.dto.legacy.AddDefendantAccountPartyLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.ContactDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountPartyLegacy;
import uk.gov.hmcts.opal.dto.legacy.EmployerDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.LanguagePreferencesLegacy;
import uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.VehicleDetailsLegacy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DefendantAccountPartyLegacyResponseMapper {

    // Mappings are mostly implicit but retained here to guard against method name changes.
    @Mappings({
        @Mapping(target = "version", source = "version", qualifiedByName = "intToBigInteger"),
        @Mapping(target = "defendantAccountParty", source = "defendantAccountParty")
    })
    GetDefendantAccountPartyResponse toDefendantAccountPartyResponse(
        AddDefendantAccountPartyLegacyResponse legacyResponse
    );

    @Mappings({
        @Mapping(target = "defendantAccountPartyType", source = "defendantAccountPartyType"),
        @Mapping(target = "isDebtor", source = "isDebtor"),
        @Mapping(target = "partyDetails", source = "partyDetails"),
        @Mapping(target = "address", source = "address"),
        @Mapping(target = "contactDetails", source = "contactDetails"),
        @Mapping(target = "vehicleDetails", source = "vehicleDetails"),
        @Mapping(target = "employerDetails", source = "employerDetails"),
        @Mapping(target = "languagePreferences", source = "languagePreferences")
    })
    DefendantAccountParty toDto(DefendantAccountPartyLegacy legacy);

    PartyDetails toDto(PartyDetailsLegacy legacy);

    AddressDetails toDto(AddressDetailsLegacy legacy);

    ContactDetails toDto(ContactDetailsLegacy legacy);

    VehicleDetails toDto(VehicleDetailsLegacy legacy);

    EmployerDetails toDto(EmployerDetailsLegacy legacy);

    @Mappings({
        @Mapping(target = "documentLanguagePreference", source = "documentLanguagePreference"),
        @Mapping(target = "hearingLanguagePreference", source = "hearingLanguagePreference"),
    })
    LanguagePreferences toDto(LanguagePreferencesLegacy legacy);

    default LanguagePreference toLanguagePreference(LanguagePreferencesLegacy.LanguagePreference legacyPreference) {
        if (legacyPreference == null) {
            return null;
        }
        return LanguagePreference.fromCode(legacyPreference.getLanguageCode());
    }

    @Named("intToBigInteger")
    default BigInteger intToBigInteger(Integer value) {
        return value == null ? null : BigInteger.valueOf(value.longValue());
    }
}
