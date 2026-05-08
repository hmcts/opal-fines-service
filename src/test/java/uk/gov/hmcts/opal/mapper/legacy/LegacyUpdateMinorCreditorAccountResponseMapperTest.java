package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateMinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyCreditorAccountPaymentDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LegacyUpdateMinorCreditorAccountResponseMapperTest.MapperTestConfig.class)
class LegacyUpdateMinorCreditorAccountResponseMapperTest {

    @Autowired
    private LegacyUpdateMinorCreditorAccountResponseMapper mapper;

    @Configuration
    @ComponentScan(basePackages = "uk.gov.hmcts.opal.mapper")
    static class MapperTestConfig {
    }

    @Test
    void toMinorCreditorAccountResponse_mapsAllCoreFields() {
        LegacyUpdateMinorCreditorAccountResponse legacy = LegacyUpdateMinorCreditorAccountResponse.builder()
            .accountVersion(2)
            .creditorAccountId(607L)
            .partyDetails(LegacyPartyDetails.builder()
                .partyId("99008")
                .organisationFlag(false)
                .organisationDetails(OrganisationDetails.builder()
                    .organisationName("Updated Ltd")
                    .organisationAliases(new OrganisationDetails.OrganisationAlias[] {
                        OrganisationDetails.OrganisationAlias.builder()
                            .aliasId("ORG-1")
                            .sequenceNumber((short) 4)
                            .organisationName("Updated Alias")
                            .build()
                    })
                    .build())
                .individualDetails(IndividualDetails.builder()
                    .title("Ms")
                    .firstNames("Creditor")
                    .surname("Updated")
                    .dateOfBirth(LocalDate.of(2000, 2, 1))
                    .age("24")
                    .nationalInsuranceNumber("QQ123456C")
                    .individualAliases(new IndividualDetails.IndividualAlias[] {
                        IndividualDetails.IndividualAlias.builder()
                            .aliasId("IND-1")
                            .sequenceNumber((short) 6)
                            .surname("AliasSurname")
                            .forenames("AliasForenames")
                            .build()
                    })
                    .build())
                .build())
            .address(AddressDetailsLegacy.builder()
                .addressLine1("99 Updated Road")
                .addressLine2("Updated Area")
                .addressLine3("Updated Town")
                .postcode("NW1 1AA")
                .build())
            .payment(LegacyCreditorAccountPaymentDetails.builder()
                .accountName("Updated Account")
                .sortCode("112233")
                .accountNumber("12345678")
                .accountReference("Ref-01")
                .payByBacs(true)
                .holdPayment(true)
                .build())
            .build();

        var mapped = mapper.toMinorCreditorAccountResponse(legacy);

        assertNotNull(mapped);
        assertEquals(BigInteger.valueOf(2L), mapped.getVersion());
        assertEquals(607L, mapped.getCreditorAccountId());

        assertNotNull(mapped.getPartyDetails());
        assertEquals("99008", mapped.getPartyDetails().getPartyId());
        assertFalse(mapped.getPartyDetails().getOrganisationFlag());
        assertNotNull(mapped.getPartyDetails().getOrganisationDetails());
        assertEquals("Updated Ltd", mapped.getPartyDetails().getOrganisationDetails().getOrganisationName());
        assertNotNull(mapped.getPartyDetails().getOrganisationDetails().getOrganisationAliases());
        assertEquals(1, mapped.getPartyDetails().getOrganisationDetails().getOrganisationAliases().size());
        assertEquals("ORG-1", mapped.getPartyDetails().getOrganisationDetails().getOrganisationAliases().getFirst()
            .getAliasId());
        assertEquals(4, mapped.getPartyDetails().getOrganisationDetails().getOrganisationAliases().getFirst()
            .getSequenceNumber());
        assertNotNull(mapped.getPartyDetails().getIndividualDetails());
        assertEquals("Ms", mapped.getPartyDetails().getIndividualDetails().getTitle());
        assertEquals("Creditor", mapped.getPartyDetails().getIndividualDetails().getForenames());
        assertEquals("Updated", mapped.getPartyDetails().getIndividualDetails().getSurname());
        assertEquals("2000-02-01", mapped.getPartyDetails().getIndividualDetails().getDateOfBirth());
        assertEquals("24", mapped.getPartyDetails().getIndividualDetails().getAge());
        assertEquals("QQ123456C", mapped.getPartyDetails().getIndividualDetails().getNationalInsuranceNumber());
        assertNotNull(mapped.getPartyDetails().getIndividualDetails().getIndividualAliases());
        assertEquals(1, mapped.getPartyDetails().getIndividualDetails().getIndividualAliases().size());
        assertEquals("IND-1", mapped.getPartyDetails().getIndividualDetails().getIndividualAliases().getFirst()
            .getAliasId());
        assertEquals(6, mapped.getPartyDetails().getIndividualDetails().getIndividualAliases().getFirst()
            .getSequenceNumber());

        assertNotNull(mapped.getAddress());
        assertEquals("99 Updated Road", mapped.getAddress().getAddressLine1());
        assertEquals("Updated Area", mapped.getAddress().getAddressLine2());
        assertEquals("Updated Town", mapped.getAddress().getAddressLine3());
        assertEquals("NW1 1AA", mapped.getAddress().getPostcode());

        assertNotNull(mapped.getPayment());
        assertEquals("Updated Account", mapped.getPayment().getAccountName());
        assertEquals("112233", mapped.getPayment().getSortCode());
        assertEquals("12345678", mapped.getPayment().getAccountNumber());
        assertEquals("Ref-01", mapped.getPayment().getAccountReference());
        assertTrue(mapped.getPayment().getPayByBacs());
        assertTrue(mapped.getPayment().getHoldPayment());
    }

    @Test
    void toMinorCreditorAccountResponse_handlesNullNestedObjects() {
        LegacyUpdateMinorCreditorAccountResponse legacy = LegacyUpdateMinorCreditorAccountResponse.builder()
            .accountVersion(1)
            .creditorAccountId(607L)
            .partyDetails(LegacyPartyDetails.builder()
                .partyId("99008")
                .organisationFlag(true)
                .organisationDetails(OrganisationDetails.builder().organisationName("Updated Ltd").build())
                .build())
            .build();

        var mapped = mapper.toMinorCreditorAccountResponse(legacy);

        assertNotNull(mapped);
        assertEquals(BigInteger.ONE, mapped.getVersion());
        assertNotNull(mapped.getPartyDetails());
        assertTrue(mapped.getPartyDetails().getOrganisationFlag());
        assertNotNull(mapped.getPartyDetails().getOrganisationDetails());
        assertEquals("Updated Ltd", mapped.getPartyDetails().getOrganisationDetails().getOrganisationName());
        assertNull(mapped.getPartyDetails().getIndividualDetails());
        assertNull(mapped.getAddress());
        assertNull(mapped.getPayment());
    }

    @Test
    void mapperConverters_handleNullAndNonNullValues() {
        assertEquals(BigInteger.valueOf(2L), mapper.integerToBigInteger(2));
        assertNull(mapper.integerToBigInteger(null));

        assertEquals(5, mapper.shortToInteger((short) 5));
        assertNull(mapper.shortToInteger(null));

        assertEquals("2024-01-02", mapper.localDateToString(LocalDate.of(2024, 1, 2)));
        assertNull(mapper.localDateToString(null));
    }
}
