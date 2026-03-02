package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;

@SpringJUnitConfig
@ContextConfiguration(classes = {
    MinorCreditorAccountResponseMapperImpl.class,
    MinorCreditorPaymentMapperImpl.class,
    AddressDetailsCommonMapperImpl.class,
    PartyDetailsCommonMapperImpl.class,
    IndividualDetailsCommonMapperImpl.class,
    OrganisationDetailsCommonMapperImpl.class
})
class MinorCreditorAccountResponseMapperTest {

    @Autowired
    private MinorCreditorAccountResponseMapper mapper;

    @Test
    void givenIndividualPartyAndAccount_whenToMinorCreditorAccountResponse_thenMapsAllFields() {
        CreditorAccountEntity.Lite account = CreditorAccountEntity.Lite.builder()
            .creditorAccountId(101L)
            .creditorAccountType(CreditorAccountType.MN)
            .bankAccountName("A NAME")
            .bankSortCode("112233")
            .bankAccountNumber("12345678")
            .bankAccountReference("REF")
            .payByBacs(true)
            .holdPayout(false)
            .build();

        PartyEntity party = PartyEntity.builder()
            .partyId(201L)
            .organisation(false)
            .surname("Smith")
            .forenames("John")
            .title("Mr")
            .addressLine1("1 Any Street")
            .addressLine2("Any Area")
            .addressLine3("Any City")
            .addressLine4("Any County")
            .addressLine5("Any Region")
            .postcode("AB1 2CD")
            .build();

        MinorCreditorAccountResponse mapped = mapper.toMinorCreditorAccountResponse(account, party);

        assertNotNull(mapped);
        assertEquals(101L, mapped.getCreditorAccountId());
        assertNotNull(mapped.getPartyDetails());
        assertEquals("201", mapped.getPartyDetails().getPartyId());
        assertEquals(false, mapped.getPartyDetails().getOrganisationFlag());
        assertNotNull(mapped.getPartyDetails().getIndividualDetails());
        assertNull(mapped.getPartyDetails().getOrganisationDetails());

        assertNotNull(mapped.getAddress());
        assertEquals("1 Any Street", mapped.getAddress().getAddressLine1());
        assertEquals("Any Area", mapped.getAddress().getAddressLine2());
        assertEquals("Any City", mapped.getAddress().getAddressLine3());
        assertEquals("Any County", mapped.getAddress().getAddressLine4());
        assertEquals("Any Region", mapped.getAddress().getAddressLine5());
        assertEquals("AB1 2CD", mapped.getAddress().getPostcode());

        assertNotNull(mapped.getPayment());
        assertEquals("A NAME", mapped.getPayment().getAccountName());
        assertEquals("112233", mapped.getPayment().getSortCode());
        assertEquals("12345678", mapped.getPayment().getAccountNumber());
        assertEquals("REF", mapped.getPayment().getAccountReference());
        assertEquals(true, mapped.getPayment().getPayByBacs());
        assertEquals(false, mapped.getPayment().getHoldPayment());
        assertNull(mapped.getVersion());
    }

    @Test
    void givenOrganisationPartyAndAccount_whenToMinorCreditorAccountResponse_thenMapsOrganisationDetails() {
        CreditorAccountEntity.Lite account = CreditorAccountEntity.Lite.builder()
            .creditorAccountId(202L)
            .creditorAccountType(CreditorAccountType.MN)
            .bankAccountName("ORG NAME")
            .bankSortCode("445566")
            .bankAccountNumber("87654321")
            .bankAccountReference("ORGREF")
            .payByBacs(false)
            .holdPayout(true)
            .build();

        PartyEntity party = PartyEntity.builder()
            .partyId(301L)
            .organisation(true)
            .organisationName("Acme Ltd")
            .addressLine1("2 Org Street")
            .addressLine2("Org Area")
            .addressLine3("Org City")
            .addressLine4("Org County")
            .addressLine5("Org Region")
            .postcode("ZZ1 9ZZ")
            .build();

        MinorCreditorAccountResponse mapped = mapper.toMinorCreditorAccountResponse(account, party);

        assertNotNull(mapped);
        assertEquals(202L, mapped.getCreditorAccountId());
        assertNotNull(mapped.getPartyDetails());
        assertEquals("301", mapped.getPartyDetails().getPartyId());
        assertTrue(mapped.getPartyDetails().getOrganisationFlag());
        assertNotNull(mapped.getPartyDetails().getOrganisationDetails());
        assertEquals("Acme Ltd", mapped.getPartyDetails().getOrganisationDetails().getOrganisationName());
        assertNull(mapped.getPartyDetails().getIndividualDetails());

        assertNotNull(mapped.getAddress());
        assertEquals("2 Org Street", mapped.getAddress().getAddressLine1());
        assertEquals("Org Area", mapped.getAddress().getAddressLine2());
        assertEquals("Org City", mapped.getAddress().getAddressLine3());
        assertEquals("Org County", mapped.getAddress().getAddressLine4());
        assertEquals("Org Region", mapped.getAddress().getAddressLine5());
        assertEquals("ZZ1 9ZZ", mapped.getAddress().getPostcode());

        assertNotNull(mapped.getPayment());
        assertEquals("ORG NAME", mapped.getPayment().getAccountName());
        assertEquals("445566", mapped.getPayment().getSortCode());
        assertEquals("87654321", mapped.getPayment().getAccountNumber());
        assertEquals("ORGREF", mapped.getPayment().getAccountReference());
        assertEquals(false, mapped.getPayment().getPayByBacs());
        assertEquals(true, mapped.getPayment().getHoldPayment());
        assertNull(mapped.getVersion());
    }
}
