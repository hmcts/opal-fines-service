package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyCreditorAccountPaymentDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyUpdateMinorCreditorAccountRequestTest {

    @Test
    void toJsonString_serializesNestedLegacyFieldsInSnakeCase() throws Exception {
        LegacyUpdateMinorCreditorAccountRequest request = LegacyUpdateMinorCreditorAccountRequest.builder()
            .creditorAccountId("607")
            .businessUnitId("10")
            .businessUnitUserId("USER01")
            .accountVersion(1)
            .partyDetails(LegacyPartyDetails.builder()
                .partyId("99008")
                .organisationFlag(true)
                .organisationDetails(OrganisationDetails.builder()
                    .organisationName("Updated Ltd")
                    .build())
                .build())
            .address(AddressDetailsLegacy.builder()
                .addressLine1("99 Updated Road")
                .addressLine2("Updated Area")
                .addressLine3("Updated Town")
                .postcode("NW1 1AA")
                .build())
            .payment(LegacyCreditorAccountPaymentDetails.builder()
                .accountReference("Ref-01")
                .holdPayment(true)
                .build())
            .build();

        JsonNode json = ToJsonString.getObjectMapper().readTree(
            ToJsonString.getObjectMapper().writeValueAsString(request)
        );

        assertEquals("607", json.get("creditor_account_id").asText());
        assertEquals("99008", json.get("party_details").get("party_id").asText());
        assertTrue(json.get("party_details").get("organisation_flag").asBoolean());
        assertEquals("Updated Ltd",
            json.get("party_details").get("organisation_details").get("organisation_name").asText());
        assertEquals("99 Updated Road", json.get("address").get("address_line_1").asText());
        assertEquals("Ref-01", json.get("payment").get("account_reference").asText());

        assertFalse(json.get("party_details").has("partyId"));
        assertFalse(json.get("party_details").has("organisationFlag"));
        assertFalse(json.get("address").has("addressLine1"));
    }
}
