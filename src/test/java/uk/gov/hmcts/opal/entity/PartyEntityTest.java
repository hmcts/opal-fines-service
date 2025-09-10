package uk.gov.hmcts.opal.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PartyEntityTest {

    @Test
    void testGettersAndSetters() {
        PartyEntity party = new PartyEntity();

        // Set values using setters
        party.setPartyId(1L);
        party.setOrganisation(true);
        party.setOrganisationName("Big Acme Corporation");
        party.setSurname("Smith");
        party.setForenames("John James");
        party.setTitle("Mr");
        party.setAddressLine1("22 Acacia Avenue");
        party.setAddressLine2("Hammersmith");
        party.setAddressLine3("Birmingham");
        party.setAddressLine4("Cornwall");
        party.setAddressLine5("Scotland");
        party.setPostcode("SN15 9TT");
        party.setAccountType("TFO");  // TFO = Transfer. Could also be FP = Fixed Penalty
        party.setBirthDate(LocalDate.of(2001, 8, 16));
        party.setAge((short)21);
        party.setNiNumber("FF22446688");
        party.setLastChangedDate(LocalDateTime.of(2023, 12, 5, 15, 45));

        // Test getters
        assertEquals(Long.valueOf(1L), party.getPartyId());
        assertTrue(party.isOrganisation());
        assertEquals("Big Acme Corporation", party.getOrganisationName());
        assertEquals("Smith", party.getSurname());
        assertEquals("John James", party.getForenames());
        assertEquals("Mr", party.getTitle());
        assertEquals("22 Acacia Avenue", party.getAddressLine1());
        assertEquals("Hammersmith", party.getAddressLine2());
        assertEquals("Birmingham", party.getAddressLine3());
        assertEquals("Cornwall", party.getAddressLine4());
        assertEquals("Scotland", party.getAddressLine5());
        assertEquals("SN15 9TT", party.getPostcode());
        assertEquals("TFO", party.getAccountType());
        assertEquals(LocalDate.of(2001, 8, 16), party.getBirthDate());
        assertEquals(Short.valueOf((short) 21), party.getAge());
        assertEquals("FF22446688", party.getNiNumber());
        assertEquals(LocalDateTime.of(2023, 12, 5, 15, 45), party.getLastChangedDate());
    }

    @Test
    void testLombokMethods() {
        PartyEntity party1 = new PartyEntity();
        party1.setPartyId(1L);

        PartyEntity party2 = new PartyEntity();
        party2.setPartyId(1L);

        PartyEntity differentParty = new PartyEntity();
        differentParty.setPartyId(2L);

        // Test equals method
        assertEquals(party1, party2);
        assertNotEquals(party1, differentParty);

        // Test hashCode method
        assertEquals(party1.hashCode(), party2.hashCode());
        assertNotEquals(party1.hashCode(), differentParty.hashCode());

        // Test toString method
        assertNotNull(party1.toString());
    }
}
