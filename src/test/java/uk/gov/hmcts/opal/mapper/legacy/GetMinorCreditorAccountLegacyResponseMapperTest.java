package uk.gov.hmcts.opal.mapper.legacy;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.CreditorAccountPaymentDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.IndividualDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy;
import uk.gov.hmcts.opal.mapper.AbstractMapperTest;

@Isolated
class GetMinorCreditorAccountLegacyResponseMapperTest extends AbstractMapperTest {

    @Autowired
    private GetMinorCreditorAccountLegacyResponseMapper mapper;

    @Test
    void toOpal_mapsLegacyResponse() {
        GetMinorCreditorAccountLegacyResponse legacyResponse = GetMinorCreditorAccountLegacyResponse.builder()
            .accountVersion(4)
            .creditorAccountId(123L)
            .partyDetails(PartyDetailsLegacy.builder()
                .partyId("200")
                .organisationFlag(false)
                .individualDetails(IndividualDetailsLegacy.builder()
                    .title("Ms")
                    .forenames("Alex")
                    .surname("Smith")
                    .dateOfBirth("1990-01-02")
                    .age("36")
                    .nationalInsuranceNumber("AB123456C")
                    .build())
                .build())
            .address(AddressDetailsLegacy.builder()
                .addressLine1("1 High Street")
                .addressLine2("Flat 2")
                .addressLine3("District")
                .addressLine4("Town")
                .addressLine5("County")
                .postcode("AB1 2CD")
                .build())
            .payment(CreditorAccountPaymentDetailsLegacy.builder()
                .accountName("A Smith")
                .sortCode("123456")
                .accountNumber("12345678")
                .accountReference("REF123")
                .payByBacs(true)
                .holdPayment(false)
                .build())
            .build();

        MinorCreditorAccountResponse opalResponse = mapper.toOpal(legacyResponse);

        assertThat(opalResponse.getVersion()).isEqualTo(BigInteger.valueOf(4));
        assertThat(opalResponse.getCreditorAccountId()).isEqualTo(123L);

        assertThat(opalResponse.getPartyDetails().getPartyId()).isEqualTo("200");
        assertThat(opalResponse.getPartyDetails().getOrganisationFlag()).isFalse();
        assertThat(opalResponse.getPartyDetails().getIndividualDetails().getTitle()).isEqualTo("Ms");
        assertThat(opalResponse.getPartyDetails().getIndividualDetails().getForenames()).isEqualTo("Alex");
        assertThat(opalResponse.getPartyDetails().getIndividualDetails().getSurname()).isEqualTo("Smith");
        assertThat(opalResponse.getPartyDetails().getIndividualDetails().getDateOfBirth()).isEqualTo("1990-01-02");
        assertThat(opalResponse.getPartyDetails().getIndividualDetails().getAge()).isEqualTo("36");
        assertThat(opalResponse.getPartyDetails().getIndividualDetails().getNationalInsuranceNumber())
            .isEqualTo("AB123456C");

        assertThat(opalResponse.getAddress().getAddressLine1()).isEqualTo("1 High Street");
        assertThat(opalResponse.getAddress().getAddressLine2()).isEqualTo("Flat 2");
        assertThat(opalResponse.getAddress().getAddressLine3()).isEqualTo("District");
        assertThat(opalResponse.getAddress().getAddressLine4()).isEqualTo("Town");
        assertThat(opalResponse.getAddress().getAddressLine5()).isEqualTo("County");
        assertThat(opalResponse.getAddress().getPostcode()).isEqualTo("AB1 2CD");

        assertThat(opalResponse.getPayment().getAccountName()).isEqualTo("A Smith");
        assertThat(opalResponse.getPayment().getSortCode()).isEqualTo("123456");
        assertThat(opalResponse.getPayment().getAccountNumber()).isEqualTo("12345678");
        assertThat(opalResponse.getPayment().getAccountReference()).isEqualTo("REF123");
        assertThat(opalResponse.getPayment().getPayByBacs()).isTrue();
        assertThat(opalResponse.getPayment().getHoldPayment()).isFalse();
    }
}
