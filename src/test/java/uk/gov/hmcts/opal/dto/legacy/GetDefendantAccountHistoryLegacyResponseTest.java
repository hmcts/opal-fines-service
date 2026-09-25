package uk.gov.hmcts.opal.dto.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.util.XmlUtil;

class GetDefendantAccountHistoryLegacyResponseTest {

    @Test
    void unmarshalXmlString_whenNoteTextIsNestedInNoteDetails_populatesNoteText() throws JAXBException {
        GetDefendantAccountHistoryLegacyResponse response = XmlUtil.unmarshalXmlString("""
            <response>
              <history_items>
                <history_items_element>
                  <type>Note</type>
                  <details>
                    <note_details>
                      <note_text>Nested legacy account note</note_text>
                    </note_details>
                  </details>
                </history_items_element>
              </history_items>
            </response>
            """, GetDefendantAccountHistoryLegacyResponse.class);

        assertEquals("Nested legacy account note",
            response.getHistoryItems().get(0).getDetails().getNoteText());
    }

    @Test
    void unmarshalXmlString_whenNoteTextIsDirectlyInDetails_populatesNoteText() throws JAXBException {
        GetDefendantAccountHistoryLegacyResponse response = XmlUtil.unmarshalXmlString("""
            <response>
              <history_items>
                <history_items_element>
                  <type>Note</type>
                  <details>
                    <note_text>Flat legacy account note</note_text>
                  </details>
                </history_items_element>
              </history_items>
            </response>
            """, GetDefendantAccountHistoryLegacyResponse.class);

        assertEquals("Flat legacy account note",
            response.getHistoryItems().get(0).getDetails().getNoteText());
    }
}
