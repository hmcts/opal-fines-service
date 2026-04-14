package uk.gov.hmcts.opal.mapper.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountAtAGlanceResponse.AtAGlanceDefendant;
import uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPayment;
import uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails;

import java.math.BigInteger;
import java.time.LocalDate;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorAccountAtAGlanceEntity;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = GetMinorCreditorAccountAtAGlanceResponseMapperTest.MapperTestConfig.class)
@Isolated
public class GetMinorCreditorAccountAtAGlanceResponseMapperTest {

    @Autowired
    private GetMinorCreditorAccountAtAGlanceResponseMapper mapper;

    @Configuration
    @ComponentScan(basePackages = "uk.gov.hmcts.opal.mapper")
    static class MapperTestConfig {
    }

    @Test
    public void testLegacyToOpalFullConversion() {

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

        AtAGlanceDefendant defendant = AtAGlanceDefendant.builder()
            .accountNumber("R3B3LS")
            .accountId(66L)
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
        assertTrue(result.getPayment().getBacs());
    }

    @Test
    public void testEntityToOpalFullConversion() {
        MinorCreditorAccountAtAGlanceEntity entity = MinorCreditorAccountAtAGlanceEntity.builder()
            .creditorId(66L)
            .accountNumber("ORDER-66")
            .addressLine1("Jedi Temple")
            .addressLine2("Galactic City")
            .addressLine3("Coruscant")
            .addressLine4("Core Worlds")
            .addressLine5("Galactic Republic")
            .postcode("C0R U5C")
            .defendantAccountId(1977L)
            .defendantAccountNumber("REB-1977")
            .defendantTitle("Master")
            .defendantForenames("Luke")
            .defendantSurname("Skywalker")
            .payByBacs(Boolean.TRUE)
            .holdPayout(Boolean.FALSE)
            .build();

        GetMinorCreditorAccountAtAGlanceResponse dto = mapper.toDto(entity, null);

        assertNotNull(dto);

        // creditor account
        assertEquals(66L, dto.getCreditorAccountId());

        // address
        assertNotNull(dto.getAddress());
        assertEquals("Jedi Temple", dto.getAddress().getAddressLine1());
        assertEquals("Galactic City", dto.getAddress().getAddressLine2());
        assertEquals("Coruscant", dto.getAddress().getAddressLine3());
        assertEquals("Core Worlds", dto.getAddress().getAddressLine4());
        assertEquals("Galactic Republic", dto.getAddress().getAddressLine5());
        assertEquals("C0R U5C", dto.getAddress().getPostcode());

        // defendant
        assertNotNull(dto.getDefendant());
        assertEquals("REB-1977", dto.getDefendant().getAccountNumber());
        assertEquals(1977L, dto.getDefendant().getAccountId());
        assertEquals("Master", dto.getDefendant().getTitle());
        assertEquals("Luke", dto.getDefendant().getForenames());
        assertEquals("Skywalker", dto.getDefendant().getSurname());

        // payment
        assertNotNull(dto.getPayment());
        assertTrue(dto.getPayment().getBacs());
        assertFalse(dto.getPayment().getHoldPayment());
    }

}
