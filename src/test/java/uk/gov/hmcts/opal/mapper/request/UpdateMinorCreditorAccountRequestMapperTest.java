package uk.gov.hmcts.opal.mapper.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.CreditorAccountPaymentDetailsCommon;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = UpdateMinorCreditorAccountRequestMapperTest.MapperTestConfig.class)
class UpdateMinorCreditorAccountRequestMapperTest {

    @Autowired
    private UpdateMinorCreditorAccountRequestMapper mapper;

    @Configuration
    @ComponentScan(basePackages = "uk.gov.hmcts.opal.mapper")
    static class MapperTestConfig {
    }

    @Test
    void toLegacyUpdateMinorCreditorAccountRequest_mapsCoreFieldsAndPaymentDetails() {
        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon()
                .partyId("99008")
                .organisationFlag(false)
                .individualDetails(new IndividualDetailsCommon()
                    .title("Ms")
                    .forenames("Creditor")
                    .surname("Updated")))
            .address(new AddressDetailsCommon()
                .addressLine1("99 Updated Road")
                .addressLine2("Updated Area")
                .addressLine3("Updated Town")
                .postcode("NW1 1AA"))
            .payment(new CreditorAccountPaymentDetailsCommon()
                .accountName("Updated Account")
                .sortCode("112233")
                .accountNumber("12345678")
                .accountReference("Ref-01")
                .payByBacs(true)
                .holdPayment(true));

        LegacyUpdateMinorCreditorAccountRequest mapped = mapper.toLegacyUpdateMinorCreditorAccountRequest(
            607L,
            (short) 10,
            "USER01",
            BigInteger.ONE,
            request
        );

        assertNotNull(mapped);
        assertEquals("607", mapped.getCreditorAccountId());
        assertEquals("10", mapped.getBusinessUnitId());
        assertEquals("USER01", mapped.getBusinessUnitUserId());
        assertEquals(1, mapped.getAccountVersion());

        assertNotNull(mapped.getPartyDetails());
        assertEquals("99008", mapped.getPartyDetails().getPartyId());
        assertEquals(false, mapped.getPartyDetails().getOrganisationFlag());
        assertNotNull(mapped.getPartyDetails().getIndividualDetails());
        assertEquals("Ms", mapped.getPartyDetails().getIndividualDetails().getTitle());
        assertEquals("Creditor", mapped.getPartyDetails().getIndividualDetails().getFirstNames());
        assertEquals("Updated", mapped.getPartyDetails().getIndividualDetails().getSurname());

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
        assertEquals(true, mapped.getPayment().getPayByBacs());
        assertEquals(true, mapped.getPayment().getHoldPayment());
    }

    @Test
    void toLegacyUpdateMinorCreditorAccountRequest_preservesNullOptionalNestedValues() {
        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon()
                .partyId("99008")
                .organisationFlag(true))
            .address(new AddressDetailsCommon()
                .addressLine1("99 Updated Road")
                .postcode("NW1 1AA"))
            .payment(new CreditorAccountPaymentDetailsCommon()
                .payByBacs(false)
                .holdPayment(false));

        LegacyUpdateMinorCreditorAccountRequest mapped = mapper.toLegacyUpdateMinorCreditorAccountRequest(
            607L,
            (short) 10,
            "USER01",
            BigInteger.TWO,
            request
        );

        assertNotNull(mapped);
        assertNull(mapped.getPartyDetails().getIndividualDetails());
        assertNull(mapped.getPartyDetails().getOrganisationDetails());
        assertNull(mapped.getPayment().getAccountName());
        assertNull(mapped.getPayment().getSortCode());
        assertNull(mapped.getPayment().getAccountNumber());
        assertNull(mapped.getPayment().getAccountReference());
        assertEquals(false, mapped.getPayment().getPayByBacs());
        assertEquals(false, mapped.getPayment().getHoldPayment());
    }
}
