package uk.gov.hmcts.opal.dto.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
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
}
