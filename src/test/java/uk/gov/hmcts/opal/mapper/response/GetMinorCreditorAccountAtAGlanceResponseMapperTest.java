package uk.gov.hmcts.opal.mapper.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.Defendant;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPayment;
import uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails;

import java.math.BigInteger;
import java.time.LocalDate;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = GetMinorCreditorAccountAtAGlanceResponseMapperTest.MapperTestConfig.class)
public class GetMinorCreditorAccountAtAGlanceResponseMapperTest {

    @Autowired
    private GetMinorCreditorAccountAtAGlanceResponseMapper mapper;

    @Configuration
    @ComponentScan(basePackages = "uk.gov.hmcts.opal.mapper")
    static class MapperTestConfig {
    }

    @Test
    public void testToOpalFullConversion() {

        //Arrange
        LegacyPartyDetails legacyParty = LegacyPartyDetails.builder()
            .partyId("theEmpire")
            .organisationFlag(true)
            .organisationDetails(OrganisationDetails.builder().organisationName("The Empire").build())
            .individualDetails(IndividualDetails.builder()
                .title("Emperor")
                .firstNames("Sheev")
                .surname("Palpatine")
                .dateOfBirth(LocalDate.of(3000, 12, 25))
                .age("Ageless")
                .nationalInsuranceNumber("66")
                .individualAliases(new IndividualDetails.IndividualAlias[] {
                    IndividualDetails.IndividualAlias.builder()
                        .aliasId("sith")
                        .sequenceNumber((short) 1)
                        .surname("Sidious")
                        .forenames("Darth")
                        .build()})
                .build())
            .build();

        AddressDetailsLegacy legacyAddress = AddressDetailsLegacy.builder()
            .addressLine1("The")
            .addressLine2("Death")
            .addressLine3("Star")
            .addressLine4("2")
            .addressLine5(null)
            .postcode("SP4 C3")
            .build();

        Defendant defendant = Defendant.builder()
            .defendantAccountId("R3B3L")
            .organisation(true)
            .organisationName("The Rebel Alliance")
            .firstnames("Luke")
            .surname("Skywalker")
            .build();

        LegacyPayment payment = LegacyPayment.builder()
            .isBacs(true)
            .holdPayment(false)
            .build();
        
        LegacyGetMinorCreditorAccountAtAGlanceResponse legacyResponse =
            LegacyGetMinorCreditorAccountAtAGlanceResponse.builder()
                .party(legacyParty)
                .address(legacyAddress)
                .creditorAccountId(66L)
                .creditorAccountVersion(BigInteger.valueOf(1))
                .defendant(defendant)
                .payment(payment)
                .errorResponse(null)
                .build();

        //Act
        GetMinorCreditorAccountAtAGlanceResponse result = mapper.toDto(legacyResponse);

        //Assert
        assertEquals(66L, result.getCreditorAccountId());
        assertEquals("theEmpire", result.getParty().getPartyId());
        assertEquals("The Empire", result.getParty().getOrganisationDetails().getOrganisationName());
        assertEquals("Sheev", result.getParty().getIndividualDetails().getForenames());
        assertEquals("Sidious", result.getParty().getIndividualDetails()
            .getIndividualAliases().getFirst().getSurname());
        assertEquals("SP4 C3", result.getAddress().getPostcode());
        assertEquals(66L, result.getDefendant().getAccountId());
        assertTrue(result.getPayment().isBacs());
    }
}
