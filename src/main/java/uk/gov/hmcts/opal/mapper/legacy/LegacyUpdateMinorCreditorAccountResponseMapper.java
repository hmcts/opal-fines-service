package uk.gov.hmcts.opal.mapper.legacy;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateMinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails;
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

@Component
public class LegacyUpdateMinorCreditorAccountResponseMapper {

    public MinorCreditorAccountResponse toMinorCreditorAccountResponse(
        LegacyUpdateMinorCreditorAccountResponse legacy
    ) {
        MinorCreditorAccountResponse response = new MinorCreditorAccountResponse();
        response.setVersion(BigInteger.valueOf(legacy.getAccountVersion().longValue()));
        response.setCreditorAccountId(legacy.getCreditorAccountId());
        response.setPartyDetails(toPartyDetailsCommon(legacy.getPartyDetails()));
        response.setAddress(toAddressDetailsCommon(legacy.getAddress()));
        response.setPayment(toPaymentCommon(legacy.getPayment()));
        return response;
    }

    private PartyDetailsCommon toPartyDetailsCommon(LegacyPartyDetails source) {
        if (source == null) {
            return null;
        }

        return new PartyDetailsCommon()
            .partyId(source.getPartyId())
            .organisationFlag(source.getOrganisationFlag())
            .organisationDetails(toOrganisationDetailsCommon(source.getOrganisationDetails()))
            .individualDetails(toIndividualDetailsCommon(source.getIndividualDetails()));
    }

    private OrganisationDetailsCommon toOrganisationDetailsCommon(OrganisationDetails source) {
        if (source == null) {
            return null;
        }

        OrganisationDetailsCommon target = new OrganisationDetailsCommon()
            .organisationName(source.getOrganisationName());

        if (source.getOrganisationAliases() != null) {
            target.setOrganisationAliases(Arrays.stream(source.getOrganisationAliases())
                .map(alias -> new OrganisationAliasCommon()
                    .aliasId(alias.getAliasId())
                    .sequenceNumber(alias.getSequenceNumber() == null ? null : alias.getSequenceNumber().intValue())
                    .organisationName(alias.getOrganisationName()))
                .collect(Collectors.toList()));
        }

        return target;
    }

    private IndividualDetailsCommon toIndividualDetailsCommon(IndividualDetails source) {
        if (source == null) {
            return null;
        }

        IndividualDetailsCommon target = new IndividualDetailsCommon()
            .title(source.getTitle())
            .forenames(source.getFirstNames())
            .surname(source.getSurname())
            .dateOfBirth(source.getDateOfBirth() == null ? null : source.getDateOfBirth().toString())
            .age(source.getAge())
            .nationalInsuranceNumber(source.getNationalInsuranceNumber());

        if (source.getIndividualAliases() != null) {
            target.setIndividualAliases(Arrays.stream(source.getIndividualAliases())
                .map(alias -> new IndividualAliasCommon()
                    .aliasId(alias.getAliasId())
                    .sequenceNumber(alias.getSequenceNumber() == null ? null : alias.getSequenceNumber().intValue())
                    .surname(alias.getSurname())
                    .forenames(alias.getForenames()))
                .collect(Collectors.toList()));
        }

        return target;
    }

    private AddressDetailsCommon toAddressDetailsCommon(AddressDetailsLegacy source) {
        if (source == null) {
            return null;
        }

        return new AddressDetailsCommon()
            .addressLine1(source.getAddressLine1())
            .addressLine2(source.getAddressLine2())
            .addressLine3(source.getAddressLine3())
            .addressLine4(source.getAddressLine4())
            .addressLine5(source.getAddressLine5())
            .postcode(source.getPostcode());
    }

    private MinorCreditorAccountResponseMinorCreditorPayment toPaymentCommon(
        LegacyCreditorAccountPaymentDetails source
    ) {
        if (source == null) {
            return null;
        }

        return new MinorCreditorAccountResponseMinorCreditorPayment()
            .accountName(source.getAccountName())
            .sortCode(source.getSortCode())
            .accountNumber(source.getAccountNumber())
            .accountReference(source.getAccountReference())
            .payByBacs(source.getPayByBacs())
            .holdPayment(source.getHoldPayment());
    }
}
