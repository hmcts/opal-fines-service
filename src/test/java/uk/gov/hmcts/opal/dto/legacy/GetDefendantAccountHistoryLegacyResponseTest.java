package uk.gov.hmcts.opal.dto.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    @Test
    void unmarshalXmlString_whenErrorResponseIsReturned_populatesLegacyError() throws JAXBException {
        GetDefendantAccountHistoryLegacyResponse response = XmlUtil.unmarshalXmlString("""
            <response>
              <error_response>
                <error_code>-6502</error_code>
                <error_message>ORA-06502: PL/SQL: numeric or value error: number precision too large</error_message>
              </error_response>
            </response>
            """, GetDefendantAccountHistoryLegacyResponse.class);

        assertEquals("-6502", response.getErrorResponse().getErrorCode());
        assertEquals("ORA-06502: PL/SQL: numeric or value error: number precision too large",
            response.getErrorResponse().getErrorMessage());
    }

    @Test
    void unmarshalXmlString_whenPaymentTermsHasInstalmentPeriod_populatesInstalmentPeriodCode() throws JAXBException {
        GetDefendantAccountHistoryLegacyResponse response = XmlUtil.unmarshalXmlString("""
            <response>
              <history_items>
                <history_items_element>
                  <type>Payment terms</type>
                  <details>
                    <payment_terms>
                      <payment_terms_type>
                        <payment_terms_type_code>I</payment_terms_type_code>
                      </payment_terms_type>
                      <instalment_period>
                        <instalment_period_code>M</instalment_period_code>
                      </instalment_period>
                      <instalment_amount>100.00</instalment_amount>
                    </payment_terms>
                  </details>
                </history_items_element>
              </history_items>
            </response>
            """, GetDefendantAccountHistoryLegacyResponse.class);

        GetDefendantAccountHistoryLegacyResponse.LegacyDefendantAccountHistoryDetails details =
            response.getHistoryItems().get(0).getDetails();

        assertNotNull(details.getPaymentTerms());
        assertEquals(LegacyInstalmentPeriod.InstalmentPeriodCode.M,
            details.getPaymentTerms().getInstalmentPeriod().getInstalmentPeriodCode());
    }
}
