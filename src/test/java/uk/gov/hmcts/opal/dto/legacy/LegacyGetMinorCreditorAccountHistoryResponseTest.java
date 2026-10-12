package uk.gov.hmcts.opal.dto.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.xml.bind.JAXBException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.util.XmlUtil;

class LegacyGetMinorCreditorAccountHistoryResponseTest {

    @Test
    void unmarshalXmlString_whenStatusDateIsDate_populatesLocalDate() throws JAXBException {
        LegacyGetMinorCreditorAccountHistoryResponse response = XmlUtil.unmarshalXmlString("""
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
            """, LegacyGetMinorCreditorAccountHistoryResponse.class);

        assertEquals(LocalDate.of(2026, 5, 20),
            response.getHistoryItems().getFirst().getDetails().getStatusDate());
    }
}
