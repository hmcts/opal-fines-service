package uk.gov.hmcts.opal.dto.legacy;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.xml.bind.JAXBException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHistoryLegacyResponse.LegacyMajorCreditorHistoryDetails;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHistoryLegacyResponse.LegacyMajorCreditorHistoryItem;
import uk.gov.hmcts.opal.util.XmlUtil;

class GetMajorCreditorAccountHistoryLegacyResponseTest {

    @Test
    void unmarshalXml_readsStatusDate() throws JAXBException {
        GetMajorCreditorAccountHistoryLegacyResponse response = XmlUtil.unmarshalXmlString("""
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
            """, GetMajorCreditorAccountHistoryLegacyResponse.class);

        assertThat(response.getHistoryItems().getFirst().getDetails().getStatusDate())
            .isEqualTo(LocalDate.of(2026, 1, 31));
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

        GetMajorCreditorAccountHistoryLegacyResponse response = ToJsonString.toClassInstance(
            json, GetMajorCreditorAccountHistoryLegacyResponse.class);
        assertThat(response.getHistoryItems().getFirst().getDetails().getStatusDate())
            .isEqualTo(LocalDate.of(2026, 1, 31));
    }

    private GetMajorCreditorAccountHistoryLegacyResponse legacyResponse() {
        return GetMajorCreditorAccountHistoryLegacyResponse.builder()
            .historyItems(List.of(LegacyMajorCreditorHistoryItem.builder()
                .type("Financial")
                .details(LegacyMajorCreditorHistoryDetails.builder()
                    .statusDate(LocalDate.of(2026, 1, 31))
                    .build())
                .build()))
            .build();
    }
}
