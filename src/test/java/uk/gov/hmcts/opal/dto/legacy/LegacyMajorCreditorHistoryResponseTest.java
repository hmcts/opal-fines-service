package uk.gov.hmcts.opal.dto.legacy;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.legacy.LegacyMajorCreditorHistoryResponse.HistoryDetails;
import uk.gov.hmcts.opal.dto.legacy.LegacyMajorCreditorHistoryResponse.HistoryItem;
import uk.gov.hmcts.opal.util.XmlUtil;

class LegacyMajorCreditorHistoryResponseTest {

    private static final String RESPONSE_SCHEMA = "legacy/majorCreditorHistoryResponse.json";

    @Test
    void unmarshalXml_readsStatusDate() throws JAXBException {
        LegacyMajorCreditorHistoryResponse response = XmlUtil.unmarshalXmlString("""
            <response>
              <history_items>
                <history_items_element>
                  <type>Financial</type>
                  <details>
                    <status_date>2026-01-31</status_date>
                  </details>
                </history_items_element>
              </history_items>
            </response>
            """, LegacyMajorCreditorHistoryResponse.class);

        assertThat(response.getHistoryItems().getFirst().getDetails().getStatusDate())
            .isEqualTo(LocalDate.of(2026, Month.JANUARY, 31));
    }

    @Test
    void marshalXml_writesStatusDateWithoutTime() throws JAXBException {
        assertThat(legacyResponse().toXmlString()).contains("<status_date>2026-01-31</status_date>");
    }

    @Test
    void json_preservesStatusDateWithoutTime() {
        String json = ToJsonString.getObjectMapper().writeValueAsString(legacyResponse());

        assertThat(ToJsonString.toJsonNode(json).at("/history_items/0/details/status_date").asString())
            .isEqualTo("2026-01-31");

        LegacyMajorCreditorHistoryResponse response = ToJsonString.toClassInstance(
            json, LegacyMajorCreditorHistoryResponse.class);
        assertThat(response.getHistoryItems().getFirst().getDetails().getStatusDate())
            .isEqualTo(LocalDate.of(2026, Month.JANUARY, 31));
    }

    @Test
    void jsonSchema_definesStatusDateAsDate() throws IOException {
        JsonNode schema = ToJsonString.getObjectMapper().readTree(
            new ClassPathResource("jsonSchemas/" + RESPONSE_SCHEMA).getInputStream());
        JsonNode statusDate = schema.at("/$defs/creditorTransactionDetails/properties/status_date");

        assertThat(statusDate.path("type").asString()).isEqualTo("string");
        assertThat(statusDate.path("format").asString()).isEqualTo("date");
    }

    private LegacyMajorCreditorHistoryResponse legacyResponse() {
        return LegacyMajorCreditorHistoryResponse.builder()
            .historyItems(List.of(HistoryItem.builder()
                .type("Financial")
                .details(HistoryDetails.builder()
                    .statusDate(LocalDate.of(2026, Month.JANUARY, 31))
                    .build())
                .build()))
            .build();
    }
}
