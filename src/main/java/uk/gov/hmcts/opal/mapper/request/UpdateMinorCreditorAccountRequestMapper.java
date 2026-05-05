package uk.gov.hmcts.opal.mapper.request;

import java.math.BigInteger;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyCreditorAccountPaymentDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.CreditorAccountPaymentDetailsCommon;
import uk.gov.hmcts.opal.generated.model.IndividualAliasCommon;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommon;
import uk.gov.hmcts.opal.generated.model.OrganisationAliasCommon;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;

@Component
public class UpdateMinorCreditorAccountRequestMapper {

    public LegacyUpdateMinorCreditorAccountRequest toLegacyUpdateMinorCreditorAccountRequest(
        Long creditorAccountId,
        Short businessUnitId,
        String businessUnitUserId,
        BigInteger accountVersion,
        PatchMinorCreditorAccountRequest request
    ) {
        return LegacyUpdateMinorCreditorAccountRequest.builder()
            .creditorAccountId(String.valueOf(creditorAccountId))
            .businessUnitId(String.valueOf(businessUnitId))
            .businessUnitUserId(businessUnitUserId)
            .accountVersion(accountVersion.intValueExact())
            .partyDetails(toLegacyPartyDetails(request.getPartyDetails()))
            .address(toLegacyAddress(request.getAddress()))
            .payment(toLegacyPayment(request.getPayment()))
            .build();
    }

    private LegacyPartyDetails toLegacyPartyDetails(PartyDetailsCommon source) {
        return LegacyPartyDetails.builder()
            .partyId(source.getPartyId())
            .organisationFlag(source.getOrganisationFlag())
            .organisationDetails(toLegacyOrganisationDetails(source.getOrganisationDetails()))
            .individualDetails(toLegacyIndividualDetails(source.getIndividualDetails()))
            .build();
    }

    private OrganisationDetails toLegacyOrganisationDetails(OrganisationDetailsCommon source) {
        if (source == null) {
            return null;
        }

        return OrganisationDetails.builder()
            .organisationName(source.getOrganisationName())
            .organisationAliases(toLegacyOrganisationAliases(source.getOrganisationAliases()))
            .build();
    }

    private OrganisationDetails.OrganisationAlias[] toLegacyOrganisationAliases(List<OrganisationAliasCommon> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }

        return source.stream()
            .map(alias -> OrganisationDetails.OrganisationAlias.builder()
                .aliasId(alias.getAliasId())
                .sequenceNumber(alias.getSequenceNumber() == null ? null : alias.getSequenceNumber().shortValue())
                .organisationName(alias.getOrganisationName())
                .build())
            .toArray(OrganisationDetails.OrganisationAlias[]::new);
    }

    private IndividualDetails toLegacyIndividualDetails(IndividualDetailsCommon source) {
        if (source == null) {
            return null;
        }

        return IndividualDetails.builder()
            .title(source.getTitle())
            .firstNames(source.getForenames())
            .surname(source.getSurname())
            .age(source.getAge())
            .nationalInsuranceNumber(source.getNationalInsuranceNumber())
            .individualAliases(toLegacyIndividualAliases(source.getIndividualAliases()))
            .build();
    }

    private IndividualDetails.IndividualAlias[] toLegacyIndividualAliases(List<IndividualAliasCommon> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }

        return source.stream()
            .map(alias -> IndividualDetails.IndividualAlias.builder()
                .aliasId(alias.getAliasId())
                .sequenceNumber(alias.getSequenceNumber() == null ? null : alias.getSequenceNumber().shortValue())
                .surname(alias.getSurname())
                .forenames(alias.getForenames())
                .build())
            .toArray(IndividualDetails.IndividualAlias[]::new);
    }

    private AddressDetailsLegacy toLegacyAddress(AddressDetailsCommon source) {
        return AddressDetailsLegacy.builder()
            .addressLine1(source.getAddressLine1())
            .addressLine2(source.getAddressLine2())
            .addressLine3(source.getAddressLine3())
            .addressLine4(source.getAddressLine4())
            .addressLine5(source.getAddressLine5())
            .postcode(source.getPostcode())
            .build();
    }

    private LegacyCreditorAccountPaymentDetails toLegacyPayment(CreditorAccountPaymentDetailsCommon source) {
        return LegacyCreditorAccountPaymentDetails.builder()
            .accountName(source.getAccountName())
            .sortCode(source.getSortCode())
            .accountNumber(source.getAccountNumber())
            .accountReference(source.getAccountReference())
            .payByBacs(source.getPayByBacs())
            .holdPayment(source.getHoldPayment())
            .build();
    }
}
