package uk.gov.hmcts.opal.dto.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.opal.dto.ToJsonString;

class LegacyPaymentTermsTest {

    @Test
    void toJsonString_serializesNestedFieldsInSnakeCase() throws Exception {
        LegacyPaymentTerms paymentTerms = LegacyPaymentTerms.builder()
            .daysInDefault(5)
            .dateDaysInDefaultImposed(LocalDate.parse("2026-08-01"))
            .extension(true)
            .reasonForExtension("reason")
            .paymentTermsType(new LegacyPaymentTermsType(LegacyPaymentTermsType.PaymentTermsTypeCode.B))
            .effectiveDate(LocalDate.parse("2026-08-09"))
            .lumpSumAmount(new BigDecimal("100.00"))
            .instalmentAmount(new BigDecimal("25.00"))
            .postedDetails(new LegacyPostedDetails(
                LocalDateTime.parse("2026-08-10T09:15:30"), "L077JG", "opal-test"))
            .build();

        JsonNode json = ToJsonString.getObjectMapper().readTree(
            ToJsonString.getObjectMapper().writeValueAsString(paymentTerms)
        );

        assertEquals(5, json.get("days_in_default").asInt());
        assertEquals("2026-08-01", json.get("date_days_in_default_imposed").asText());
        assertEquals("reason", json.get("reason_for_extension").asText());
        assertEquals("B", json.get("payment_terms_type").get("payment_terms_type_code").asText());
        assertEquals("2026-08-09", json.get("effective_date").asText());
        assertEquals(100.00, json.get("lump_sum_amount").asDouble());
        assertEquals(25.00, json.get("instalment_amount").asDouble());
        assertEquals("L077JG", json.get("posted_details").get("posted_by").asText());
        assertEquals("opal-test", json.get("posted_details").get("posted_by_name").asText());

        assertFalse(json.has("daysInDefault"));
        assertFalse(json.has("dateDaysInDefaultImposed"));
        assertFalse(json.has("reasonForExtension"));
        assertFalse(json.has("paymentTermsType"));
        assertFalse(json.has("effectiveDate"));
        assertFalse(json.has("lumpSumAmount"));
        assertFalse(json.has("instalmentAmount"));
        assertFalse(json.has("postedDetails"));
        assertFalse(json.get("payment_terms_type").has("paymentTermsTypeCode"));
        assertFalse(json.get("posted_details").has("postedBy"));
        assertFalse(json.get("posted_details").has("postedByName"));
    }
}
