package uk.gov.hmcts.opal.dto.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.dto.legacy.MinorCreditorHistoryLegacyResponse.LegacyMinorCreditorAccountHistoryItem;
import uk.gov.hmcts.opal.dto.legacy.MinorCreditorHistoryLegacyResponse.LegacyMinorCreditorHistoryDetails;
import uk.gov.hmcts.opal.util.XmlUtil;

class MinorCreditorHistoryLegacyResponseTest {

    private static final String RESPONSE_SCHEMA =
        "legacy/getMinorCreditorAccountHistoryLegacyResponse.json";

    @Test
    void unmarshalXmlString_whenStatusDateIsDate_populatesLocalDate() throws JAXBException {
        MinorCreditorHistoryLegacyResponse response = XmlUtil.unmarshalXmlString("""
            <response>
              <history_items>
                <history_items_element>
                  <type>Financial</type>
                  <details>
                    <status_date>2026-05-20</status_date>
                  </details>
                </history_items_element>
              </history_items>
            </response>
            """, MinorCreditorHistoryLegacyResponse.class);

        assertEquals(LocalDate.of(2026, 5, 20),
            response.getHistoryItems().getFirst().getDetails().getStatusDate());
    }

    @Test
    void legacyResponseSchema_definesStatusDateAsDate() throws IOException {
        JsonNode schema = new ObjectMapper().readTree(
            new ClassPathResource("jsonSchemas/" + RESPONSE_SCHEMA).getInputStream());

        assertEquals("date", schema.at("/$defs/creditorTransactionDetails/properties/status_date/format").asText());
    }

    @Test
    void unmarshalXmlString_populatesWithNestedDetails() throws JAXBException {
        MinorCreditorHistoryLegacyResponse response = XmlUtil.unmarshalXmlString("""
            <response>
                <history_items>
                    <history_items_element>
                        <type>Financial</type>
                        <details>
                            <creditor_transaction_details>
                            <transaction_type>
                                <transaction_type>PAYMNT</transaction_type>
                                <transaction_type_display_name>Payment</transaction_type_display_name>
                            </transaction_type>
                            <payment_reference>PAY-1</payment_reference>
                            <status_date>2026-05-20</status_date>
                            </creditor_transaction_details>
                        </details>
                    </history_items_element>
                    <history_items_element>
                        <type>Note</type>
                        <details>
                            <note_details>
                                <note_text>Nested legacy note</note_text>
                            </note_details>
                        </details>
                    </history_items_element>
                </history_items>
            </response>
            """, MinorCreditorHistoryLegacyResponse.class);

        LegacyMinorCreditorHistoryDetails financial = response.getHistoryItems().getFirst()
            .getDetails().getCreditorTransactionDetails();

        assertEquals("PAYMNT", financial.getTransactionType().getTransactionType());
        assertEquals("PAY-1", financial.getPaymentReference());
        assertEquals(LocalDate.of(2026, 5, 20), financial.getStatusDate());

        LegacyMinorCreditorAccountHistoryItem note = response.getHistoryItems().get(1);
        assertEquals("Note", note.getType());
        assertEquals("Nested legacy note", note.getDetails().getNoteDetails().getNoteText());
    }
}
