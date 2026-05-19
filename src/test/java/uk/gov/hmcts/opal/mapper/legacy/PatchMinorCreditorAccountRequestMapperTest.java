package uk.gov.hmcts.opal.mapper.legacy;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.opal.dto.legacy.UpdateMinorCreditorAccountLegacyRequest;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.CreditorAccountPaymentDetailsCommon;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;

@SpringJUnitConfig
@ContextConfiguration(classes = {
    PatchMinorCreditorAccountRequestMapperImpl.class,
    PartyDetailsCommonMapperImpl.class,
    OrganisationDetailsCommonMapperImpl.class,
    IndividualDetailsCommonMapperImpl.class,
    AddressDetailsCommonMapperImpl.class,
    CreditorAccountPaymentDetailsCommonMapperImpl.class
})
@Isolated
class PatchMinorCreditorAccountRequestMapperTest {

    @Autowired
    private PatchMinorCreditorAccountRequestMapper mapper;

    @Test
    void toLegacyRequest_mapsRequestAndContextFields() {
        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon()
                .partyId("200")
                .organisationFlag(false)
                .individualDetails(new IndividualDetailsCommon()
                    .title("Mr")
                    .forenames("Alex")
                    .surname("Smith")
                    .dateOfBirth("1990-01-02")
                    .age("36")
                    .nationalInsuranceNumber("AB123456C")))
            .address(new AddressDetailsCommon()
                .addressLine1("1 High Street")
                .addressLine2("Flat 2")
                .addressLine3("District")
                .addressLine4("Town")
                .addressLine5("County")
                .postcode("AB1 2CD"))
            .payment(new CreditorAccountPaymentDetailsCommon()
                .accountName("A Smith")
                .sortCode("123456")
                .accountNumber("12345678")
                .accountReference("REF123")
                .payByBacs(true)
                .holdPayment(false));

        UpdateMinorCreditorAccountLegacyRequest legacyRequest = mapper.toLegacyRequest(
            123L,
            request,
            BigInteger.valueOf(7),
            "BU_USER_1",
            (short) 10
        );

        assertThat(legacyRequest.getCreditorAccountId()).isEqualTo("123");
        assertThat(legacyRequest.getAccountVersion()).isEqualTo(7);
        assertThat(legacyRequest.getBusinessUnitId()).isEqualTo("10");
        assertThat(legacyRequest.getBusinessUnitUserId()).isEqualTo("BU_USER_1");

        assertThat(legacyRequest.getPartyDetails().getPartyId()).isEqualTo("200");
        assertThat(legacyRequest.getPartyDetails().getOrganisationFlag()).isFalse();
        assertThat(legacyRequest.getPartyDetails().getIndividualDetails().getTitle()).isEqualTo("Mr");
        assertThat(legacyRequest.getPartyDetails().getIndividualDetails().getForenames()).isEqualTo("Alex");
        assertThat(legacyRequest.getPartyDetails().getIndividualDetails().getSurname()).isEqualTo("Smith");
        assertThat(legacyRequest.getPartyDetails().getIndividualDetails().getDateOfBirth()).isEqualTo("1990-01-02");
        assertThat(legacyRequest.getPartyDetails().getIndividualDetails().getAge()).isEqualTo("36");
        assertThat(legacyRequest.getPartyDetails().getIndividualDetails().getNationalInsuranceNumber())
            .isEqualTo("AB123456C");

        assertThat(legacyRequest.getAddress().getAddressLine1()).isEqualTo("1 High Street");
        assertThat(legacyRequest.getAddress().getAddressLine2()).isEqualTo("Flat 2");
        assertThat(legacyRequest.getAddress().getAddressLine3()).isEqualTo("District");
        assertThat(legacyRequest.getAddress().getAddressLine4()).isEqualTo("Town");
        assertThat(legacyRequest.getAddress().getAddressLine5()).isEqualTo("County");
        assertThat(legacyRequest.getAddress().getPostcode()).isEqualTo("AB1 2CD");

        assertThat(legacyRequest.getPayment().getAccountName()).isEqualTo("A Smith");
        assertThat(legacyRequest.getPayment().getSortCode()).isEqualTo("123456");
        assertThat(legacyRequest.getPayment().getAccountNumber()).isEqualTo("12345678");
        assertThat(legacyRequest.getPayment().getAccountReference()).isEqualTo("REF123");
        assertThat(legacyRequest.getPayment().getPayByBacs()).isTrue();
        assertThat(legacyRequest.getPayment().getHoldPayment()).isFalse();
    }
}
