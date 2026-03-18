package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorAccountHeaderEntity;

@SpringJUnitConfig
@ContextConfiguration(classes = {
    MinorCreditorAccountHeaderSummaryMapperImpl.class
})
class MinorCreditorAccountHeaderSummaryMapperTest {

    @Autowired
    private MinorCreditorAccountHeaderSummaryMapper mapper;

    @Test
    void givenIndividualEntity_whenToResponse_thenMapsExpectedFields() {
        // Arrange
        MinorCreditorAccountHeaderEntity entity = MinorCreditorAccountHeaderEntity.builder()
            .creditorAccountId(101L)
            .creditorAccountNumber("123")
            .creditorAccountType(CreditorAccountType.MN)
            .versionNumber(8L)
            .partyId(999L)
            .title("Ms")
            .forenames("Jane")
            .surname("Doe")
            .organisation(false)
            .businessUnitId((short) 12)
            .businessUnitName("BU Name")
            .welshLanguage(true)
            .awarded(new BigDecimal("10.00"))
            .paidOut(new BigDecimal("2.00"))
            .awaitingPayment(new BigDecimal("1.00"))
            .outstanding(BigDecimal.ZERO)
            .build();

        // Act
        GetMinorCreditorAccountHeaderSummaryResponse mapped = mapper.toResponse(entity);

        // Assert
        assertNotNull(mapped);
        assertEquals("101", mapped.getCreditorAccountId());
        assertEquals("123", mapped.getAccountNumber());
        assertEquals(BigInteger.valueOf(8L), mapped.getVersion());
        assertNotNull(mapped.getCreditorAccountType());
        assertEquals("MN", mapped.getCreditorAccountType().getType());
        assertEquals("Minor Creditor", mapped.getCreditorAccountType().getDisplayName());
        assertNotNull(mapped.getBusinessUnitSummary());
        assertEquals("12", mapped.getBusinessUnitSummary().getBusinessUnitId());
        assertEquals("BU Name", mapped.getBusinessUnitSummary().getBusinessUnitName());
        assertEquals("Y", mapped.getBusinessUnitSummary().getWelshSpeaking());
        assertNotNull(mapped.getPartyDetails());
        assertEquals("999", mapped.getPartyDetails().getPartyId());
        assertFalse(mapped.getPartyDetails().getOrganisationFlag());
        assertNotNull(mapped.getPartyDetails().getIndividualDetails());
        assertEquals("Ms", mapped.getPartyDetails().getIndividualDetails().getTitle());
        assertEquals("Jane", mapped.getPartyDetails().getIndividualDetails().getForenames());
        assertEquals("Doe", mapped.getPartyDetails().getIndividualDetails().getSurname());
        assertNull(mapped.getPartyDetails().getOrganisationDetails());
        assertTrue(mapped.getHasAssociatedDefendant());
    }

    @Test
    void givenOrganisationEntityWithoutBalances_whenToResponse_thenMapsOrganisationAndFalseAssociation() {
        // Arrange
        MinorCreditorAccountHeaderEntity entity = MinorCreditorAccountHeaderEntity.builder()
            .creditorAccountId(202L)
            .creditorAccountNumber("101")
            .creditorAccountType(CreditorAccountType.CF)
            .partyId(303L)
            .organisation(true)
            .organisationName("Some Org Ltd")
            .businessUnitId((short) 44)
            .businessUnitName("Some BU Name")
            .welshLanguage(false)
            .build();

        // Act
        GetMinorCreditorAccountHeaderSummaryResponse mapped = mapper.toResponse(entity);

        // Assert
        assertNotNull(mapped.getCreditorAccountType());
        assertEquals("CF", mapped.getCreditorAccountType().getType());
        assertEquals("Central Fund", mapped.getCreditorAccountType().getDisplayName());
        assertNotNull(mapped.getPartyDetails().getOrganisationDetails());
        assertEquals("Some Org Ltd", mapped.getPartyDetails().getOrganisationDetails().getOrganisationName());
        assertNull(mapped.getPartyDetails().getIndividualDetails());
        assertEquals("N", mapped.getBusinessUnitSummary().getWelshSpeaking());
        assertFalse(mapped.getHasAssociatedDefendant());
    }

    @Test
    void givenMajorCreditorType_whenToResponse_thenMapsMajorCreditorDisplayName() {
        // Arrange
        MinorCreditorAccountHeaderEntity entity = MinorCreditorAccountHeaderEntity.builder()
            .creditorAccountId(303L)
            .creditorAccountNumber("202")
            .creditorAccountType(CreditorAccountType.MJ)
            .partyId(404L)
            .organisation(true)
            .organisationName("Major Creditor Ltd")
            .businessUnitId((short) 55)
            .businessUnitName("Major BU Name")
            .welshLanguage(false)
            .build();

        // Act
        GetMinorCreditorAccountHeaderSummaryResponse mapped = mapper.toResponse(entity);

        // Assert
        assertNotNull(mapped.getCreditorAccountType());
        assertEquals("MJ", mapped.getCreditorAccountType().getType());
        assertEquals("Major Creditor", mapped.getCreditorAccountType().getDisplayName());
    }

    @Test
    void givenOutstandingPositiveAndAwardedNull_whenToResponse_thenHasAssociatedDefendantTrue() {
        // Arrange
        MinorCreditorAccountHeaderEntity entity = MinorCreditorAccountHeaderEntity.builder()
            .creditorAccountId(404L)
            .creditorAccountNumber("303")
            .creditorAccountType(CreditorAccountType.MN)
            .partyId(505L)
            .organisation(false)
            .title("Mr")
            .forenames("John")
            .surname("Smith")
            .businessUnitId((short) 66)
            .businessUnitName("Another BU Name")
            .welshLanguage(false)
            .awarded(null)
            .paidOut(BigDecimal.ZERO)
            .awaitingPayment(BigDecimal.ZERO)
            .outstanding(new BigDecimal("1.00"))
            .build();

        // Act
        GetMinorCreditorAccountHeaderSummaryResponse mapped = mapper.toResponse(entity);

        // Assert
        assertTrue(mapped.getHasAssociatedDefendant());
    }
}
