package uk.gov.hmcts.opal.mapper.response;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueMappingStrategy;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.debtordetail.Language;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.IndividualAliasCommonStrict;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.LanguagePreferenceCommonStrict;
import uk.gov.hmcts.opal.generated.model.LanguagePreferencesCommonStrict;
import uk.gov.hmcts.opal.generated.model.OrganisationAliasCommon;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.PartyContactDetailsDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountParty;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.PartyEmployerDetailsDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PartyVehicleDetailsDefendantAccount;

@Mapper(componentModel = "spring")
public interface DefendantAccountPartyEntityResponseMapper {

    @Mapping(target = "defendantAccountPartyType", source = "association.associationType.label",
             qualifiedByName = "toPartyType")
    @Mapping(target = "isDebtor", source = "association.debtor")
    @Mapping(target = "partyDetails", source = "association.party", qualifiedByName = "toPartyDetails")
    @Mapping(target = "address", source = "association.party", qualifiedByName = "toAddress")
    @Mapping(target = "contactDetails", source = "association.party", qualifiedByName = "toContactDetails")
    @Mapping(target = "vehicleDetails", expression = "java(toVehicleDetails(debtorDetail))")
    @Mapping(target = "employerDetails", expression = "java(toEmployerDetails(debtorDetail))")
    @Mapping(target = "languagePreferences", expression = "java(toLanguagePreferences(debtorDetail))")
    DefendantAccountParty toGeneratedResponse(DefendantAccountPartiesEntity association,
                                              DebtorDetailEntity debtorDetail,
                                              @Context List<AliasEntity> aliases);

    @Named("toPartyDetails")
    @Mapping(target = "organisationFlag", source = "organisation")
    @Mapping(target = "organisationDetails", source = ".", qualifiedByName = "toOrganisationDetails")
    @Mapping(target = "individualDetails", source = ".", qualifiedByName = "toIndividualDetails")
    PartyDetailsCommonStrict toPartyDetails(PartyEntity party, @Context List<AliasEntity> aliases);

    @Mapping(target = "organisationAliases", expression = "java(toNullableOrganisationAliases(aliases))")
    OrganisationDetailsCommonStrict mapOrganisationDetails(PartyEntity party, @Context List<AliasEntity> aliases);

    @Mapping(target = "dateOfBirth", source = "party.birthDate")
    @Mapping(target = "nationalInsuranceNumber", source = "party.niNumber")
    @Mapping(target = "individualAliases", expression = "java(toNullableIndividualAliases(aliases))")
    IndividualDetailsCommonStrict mapIndividualDetails(PartyEntity party, @Context List<AliasEntity> aliases);

    OrganisationAliasCommon toOrganisationAlias(AliasEntity alias);

    List<OrganisationAliasCommon> toOrganisationAliases(List<AliasEntity> aliases);

    IndividualAliasCommonStrict toIndividualAlias(AliasEntity alias);

    List<IndividualAliasCommonStrict> toIndividualAliases(List<AliasEntity> aliases);

    @Named("toAddress")
    AddressDetailsCommonStrict toAddress(PartyEntity party);

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    @Mapping(target = "addressLine1", source = "employerAddressLine1", qualifiedByName = "toRequiredAddressLine1")
    @Mapping(target = "addressLine2", source = "employerAddressLine2")
    @Mapping(target = "addressLine3", source = "employerAddressLine3")
    @Mapping(target = "addressLine4", source = "employerAddressLine4")
    @Mapping(target = "addressLine5", source = "employerAddressLine5")
    @Mapping(target = "postcode", source = "employerPostcode")
    AddressDetailsCommonStrict toEmployerAddress(DebtorDetailEntity debtorDetail);

    PartyContactDetailsDefendantAccount mapContactDetails(PartyEntity party);

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    @Mapping(target = "vehicleMakeAndModel", source = "vehicleMake")
    PartyVehicleDetailsDefendantAccount mapVehicleDetails(DebtorDetailEntity debtorDetail);

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    @Mapping(target = "employerReference", source = "employeeReference")
    @Mapping(target = "employerEmailAddress", source = "employerEmail")
    @Mapping(target = "employerTelephoneNumber", source = "employerTelephone")
    @Mapping(target = "employerAddress", source = ".")
    PartyEmployerDetailsDefendantAccount mapEmployerDetails(DebtorDetailEntity debtorDetail);

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    @Mapping(target = "documentLanguagePreference", source = "documentLanguage")
    @Mapping(target = "hearingLanguagePreference", source = "hearingLanguage")
    LanguagePreferencesCommonStrict mapLanguagePreferences(DebtorDetailEntity debtorDetail);

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    @Mapping(target = "languageCode", source = "code", qualifiedByName = "toLanguageCode")
    @Mapping(target = "languageDisplayName", source = "code", qualifiedByName = "toLanguageDisplayName")
    LanguagePreferenceCommonStrict toLanguagePreference(Language language);

    @Named("toPartyType")
    default DefendantAccountParty.DefendantAccountPartyTypeEnum toPartyType(String partyType) {
        return partyType == null ? null : DefendantAccountParty.DefendantAccountPartyTypeEnum.fromValue(partyType);
    }

    @Named("toOrganisationDetails")
    default JsonNullable<OrganisationDetailsCommonStrict> toOrganisationDetails(
        PartyEntity party, @Context List<AliasEntity> aliases) {
        return JsonNullable.of(party.isOrganisation() ? mapOrganisationDetails(party, aliases) : null);
    }

    @Named("toIndividualDetails")
    default JsonNullable<IndividualDetailsCommonStrict> toIndividualDetails(
        PartyEntity party, @Context List<AliasEntity> aliases) {
        return JsonNullable.of(party.isOrganisation() ? null : mapIndividualDetails(party, aliases));
    }

    @Named("toContactDetails")
    default JsonNullable<PartyContactDetailsDefendantAccount> toContactDetails(PartyEntity party) {
        return JsonNullable.of(mapContactDetails(party));
    }

    @Named("toVehicleDetails")
    default JsonNullable<PartyVehicleDetailsDefendantAccount> toVehicleDetails(DebtorDetailEntity debtorDetail) {
        return JsonNullable.of(mapVehicleDetails(orEmpty(debtorDetail)));
    }

    @Named("toEmployerDetails")
    default JsonNullable<PartyEmployerDetailsDefendantAccount> toEmployerDetails(DebtorDetailEntity debtorDetail) {
        return JsonNullable.of(mapEmployerDetails(orEmpty(debtorDetail)));
    }

    @Named("toLanguagePreferences")
    default JsonNullable<LanguagePreferencesCommonStrict> toLanguagePreferences(DebtorDetailEntity debtorDetail) {
        return JsonNullable.of(mapLanguagePreferences(orEmpty(debtorDetail)));
    }

    private static DebtorDetailEntity orEmpty(DebtorDetailEntity debtorDetail) {
        return debtorDetail == null ? new DebtorDetailEntity() : debtorDetail;
    }

    default JsonNullable<List<OrganisationAliasCommon>> toNullableOrganisationAliases(List<AliasEntity> aliases) {
        List<AliasEntity> filteredAliases = aliases == null ? List.of() : aliases.stream()
            .filter(alias -> alias.getOrganisationName() != null && !alias.getOrganisationName().isBlank())
            .sorted(Comparator.comparing(AliasEntity::getSequenceNumber))
            .toList();
        List<OrganisationAliasCommon> mappedAliases = toOrganisationAliases(filteredAliases);
        return JsonNullable.of(mappedAliases.isEmpty() ? null : mappedAliases);
    }

    default JsonNullable<List<IndividualAliasCommonStrict>> toNullableIndividualAliases(List<AliasEntity> aliases) {
        List<AliasEntity> filteredAliases = aliases == null ? List.of() : aliases.stream()
            .filter(alias -> alias.getSurname() != null && !alias.getSurname().isBlank())
            .sorted(Comparator.comparing(AliasEntity::getSequenceNumber))
            .toList();
        List<IndividualAliasCommonStrict> mappedAliases = toIndividualAliases(filteredAliases);
        return JsonNullable.of(mappedAliases.isEmpty() ? null : mappedAliases);
    }

    default <T> JsonNullable<T> toJsonNullable(T value) {
        return JsonNullable.of(value);
    }

    default JsonNullable<String> toJsonNullable(LocalDate value) {
        return JsonNullable.of(value == null ? null : value.toString());
    }

    default JsonNullable<String> toJsonNullable(Short value) {
        return JsonNullable.of(value == null ? null : value.toString());
    }

    default JsonNullable<LanguagePreferenceCommonStrict> toJsonNullable(Language language) {
        return JsonNullable.of(toLanguagePreference(language));
    }

    @Named("toRequiredAddressLine1")
    default String toRequiredAddressLine1(String addressLine1) {
        return addressLine1 == null ? "" : addressLine1;
    }

    @Named("toLanguageCode")
    default LanguagePreferenceCommonStrict.LanguageCodeEnum toLanguageCode(String languageCode) {
        return languageCode == null ? null
            : LanguagePreferenceCommonStrict.LanguageCodeEnum.fromValue(languageCode);
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
