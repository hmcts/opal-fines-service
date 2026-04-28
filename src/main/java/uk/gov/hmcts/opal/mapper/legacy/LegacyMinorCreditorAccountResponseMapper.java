package uk.gov.hmcts.opal.mapper.legacy;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyCreditorAccountPaymentDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.IndividualAliasCommon;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommon;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountResponseMinorCreditorPayment;
import uk.gov.hmcts.opal.generated.model.OrganisationAliasCommon;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true))
public interface LegacyMinorCreditorAccountResponseMapper {

    @Mapping(target = "creditorAccountId", source = "creditorAccountId")
    @Mapping(target = "partyDetails", source = "partyDetails")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "payment", source = "payment")
    @Mapping(target = "version", expression = "java(toVersion(response))")
    MinorCreditorAccountResponse toMinorCreditorAccountResponse(LegacyGetMinorCreditorAccountResponse response);

    default BigInteger toVersion(LegacyGetMinorCreditorAccountResponse response) {
        return response == null || response.getAccountVersion() == null
            ? null
            : BigInteger.valueOf(response.getAccountVersion());
    }

    @Mapping(target = "organisationFlag", source = "organisationFlag")
    @Mapping(target = "organisationDetails", expression = "java(toOrganisationDetailsCommon(legacy))")
    @Mapping(target = "individualDetails", expression = "java(toIndividualDetailsCommon(legacy))")
    PartyDetailsCommon toPartyDetailsCommon(LegacyPartyDetails legacy);

    AddressDetailsCommon toAddressDetailsCommon(AddressDetailsLegacy legacy);

    MinorCreditorAccountResponseMinorCreditorPayment toMinorCreditorPayment(
        LegacyCreditorAccountPaymentDetails paymentDetails
    );

    default OrganisationDetailsCommon toOrganisationDetailsCommon(LegacyPartyDetails legacy) {
        if (legacy == null || !Boolean.TRUE.equals(legacy.getOrganisationFlag())) {
            return null;
        }
        OrganisationDetails organisationDetails = legacy.getOrganisationDetails();
        if (organisationDetails == null) {
            return null;
        }

        return OrganisationDetailsCommon.builder()
            .organisationName(organisationDetails.getOrganisationName())
            .organisationAliases(toOrganisationAliases(organisationDetails.getOrganisationAliases()))
            .build();
    }

    default IndividualDetailsCommon toIndividualDetailsCommon(LegacyPartyDetails legacy) {
        if (legacy == null || Boolean.TRUE.equals(legacy.getOrganisationFlag())) {
            return null;
        }
        uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails individualDetails = legacy.getIndividualDetails();
        if (individualDetails == null) {
            return null;
        }

        return IndividualDetailsCommon.builder()
            .title(individualDetails.getTitle())
            .forenames(individualDetails.getFirstNames())
            .surname(individualDetails.getSurname())
            .dateOfBirth(individualDetails.getDateOfBirth() == null ? null
                : individualDetails.getDateOfBirth().toString())
            .age(individualDetails.getAge())
            .nationalInsuranceNumber(individualDetails.getNationalInsuranceNumber())
            .individualAliases(toIndividualAliases(individualDetails.getIndividualAliases()))
            .build();
    }

    default List<OrganisationAliasCommon> toOrganisationAliases(OrganisationDetails.OrganisationAlias[] aliases) {
        if (aliases == null) {
            return null;
        }
        return Arrays.stream(aliases)
            .map(alias -> OrganisationAliasCommon.builder()
                .aliasId(alias.getAliasId())
                .sequenceNumber(alias.getSequenceNumber() == null ? null : Integer.valueOf(alias.getSequenceNumber()))
                .organisationName(alias.getOrganisationName())
                .build())
            .toList();
    }

    default List<IndividualAliasCommon> toIndividualAliases(
        uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails.IndividualAlias[] aliases
    ) {
        if (aliases == null) {
            return null;
        }
        return Arrays.stream(aliases)
            .map(alias -> IndividualAliasCommon.builder()
                .aliasId(alias.getAliasId())
                .sequenceNumber(alias.getSequenceNumber() == null ? null : Integer.valueOf(alias.getSequenceNumber()))
                .surname(alias.getSurname())
                .forenames(alias.getForenames())
                .build())
            .toList();
    }
}
