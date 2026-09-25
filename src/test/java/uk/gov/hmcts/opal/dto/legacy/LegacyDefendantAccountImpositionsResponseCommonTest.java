package uk.gov.hmcts.opal.dto.legacy;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.xml.bind.JAXBException;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.util.XmlUtil;

class LegacyDefendantAccountImpositionsResponseCommonTest {

    @Test
    void whenXmlContainsUpdatedCommonObjects_unmarshalMapsValues_happyPath() throws JAXBException {
        LegacyDefendantAccountImpositionsResponseCommon response = XmlUtil.unmarshalXmlString("""
            <response>
              <version>9223372036854775808</version>
              <impositions>
                <creditor>
                  <creditor_account_type_reference>
                    <creditor_account_type>MN</creditor_account_type>
                  </creditor_account_type_reference>
                  <creditor_account_id>99000000000806</creditor_account_id>
                  <minor_creditor_organisation_flag>true</minor_creditor_organisation_flag>
                  <company_name>
                    <organisation_name>Metropolitan Traffic Unit</organisation_name>
                  </company_name>
                </creditor>
                <offence>
                  <offence_id>5510</offence_id>
                  <cjs_code>OFF0006</cjs_code>
                  <offence_title>Test Offence 6</offence_title>
                </offence>
              </impositions>
            </response>
            """, LegacyDefendantAccountImpositionsResponseCommon.class);

        LegacyDefendantAccountImpositionCommon imposition = response.getImpositions().getFirst();

        assertAll(
            () -> assertEquals(new BigInteger("9223372036854775808"), response.getVersion()),
            () -> assertEquals("MN",
                               imposition.getCreditor().getCreditorAccountTypeReference().getCreditorAccountType()),
            () -> assertEquals(99000000000806L, imposition.getCreditor().getCreditorAccountId()),
            () -> assertEquals("Metropolitan Traffic Unit",
                               imposition.getCreditor().getCompanyName().getOrganisationName()),
            () -> assertEquals(5510L, imposition.getOffence().getOffenceId()),
            () -> assertEquals("OFF0006", imposition.getOffence().getCjsCode()),
            () -> assertEquals("Test Offence 6", imposition.getOffence().getOffenceTitle())
        );
    }

}
