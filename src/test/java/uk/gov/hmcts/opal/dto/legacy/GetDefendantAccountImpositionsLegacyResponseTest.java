package uk.gov.hmcts.opal.dto.legacy;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.xml.bind.JAXBException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.Creditor;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.Imposition;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.Offence;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.PostedDetails;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.Result;
import uk.gov.hmcts.opal.util.XmlUtil;

class GetDefendantAccountImpositionsLegacyResponseTest {

    @Test
    void whenXmlResponseIsRead_populatesAllFields_happyPath() throws JAXBException {
        GetDefendantAccountImpositionsLegacyResponse response = XmlUtil.unmarshalXmlString("""
            <response>
                <version>18338687664539878704807506660830801130000030349</version>
                <impositions>
                    <impositions_element>
                        <posted_details>
                            <posted_date>2026-08-19 00:00:00.00001</posted_date>
                            <posted_by>L077AO</posted_by>
                            <posted_by_name>L077AO</posted_by_name>
                        </posted_details>
                        <result>
                            <result_id>FO</result_id>
                            <result_title>FINE</result_title>
                        </result>
                        <creditor>
                            <creditor_account_type>
                                <creditor_account_type>CF</creditor_account_type>
                            </creditor_account_type>
                            <creditor_account_id>77</creditor_account_id>
                            <major_creditor_name>HM Courts &amp; Tribunals Service</major_creditor_name>
                        </creditor>
                        <imposed_amount>-250.00</imposed_amount>
                        <paid_amount>300.00</paid_amount>
                        <balance>50.00</balance>
                        <date_imposed>2025-05-15</date_imposed>
                        <offence>
                            <offence_id>33369</offence_id>
                            <cjs_code>HY35014</cjs_code>
                            <offence_title>Riding a bicycle on a footpath</offence_title>
                        </offence>
                        <imposition_id>770000027211</imposition_id>
                    </impositions_element>
                </impositions>
            </response>
            """, GetDefendantAccountImpositionsLegacyResponse.class);

        Imposition imposition = response.getImpositions().getFirst();

        assertAll(
            () -> assertEquals(new BigInteger("18338687664539878704807506660830801130000030349"),
                               response.getVersion()),
            () -> assertEquals(1, response.getImpositions().size()),
            () -> assertPostedDetails(imposition.getPostedDetails()),
            () -> assertResult(imposition.getResult()),
            () -> assertCreditor(imposition.getCreditor()),
            () -> assertEquals(new BigDecimal("-250.00"), imposition.getImposedAmount()),
            () -> assertEquals(new BigDecimal("300.00"), imposition.getPaidAmount()),
            () -> assertEquals(new BigDecimal("50.00"), imposition.getBalance()),
            () -> assertEquals(LocalDate.parse("2025-05-15"), imposition.getDateImposed()),
            () -> assertOffence(imposition.getOffence()),
            () -> assertEquals(770000027211L, imposition.getImpositionId())
        );
    }

    private void assertPostedDetails(PostedDetails postedDetails) {
        assertAll(
            () -> assertEquals(LocalDateTime.parse("2026-08-19T00:00:00.00001"), postedDetails.getPostedDate()),
            () -> assertEquals("L077AO", postedDetails.getPostedBy()),
            () -> assertEquals("L077AO", postedDetails.getPostedByName())
        );
    }

    private void assertResult(Result result) {
        assertAll(
            () -> assertEquals("FO", result.getResultId()),
            () -> assertEquals("FINE", result.getResultTitle())
        );
    }

    private void assertCreditor(Creditor creditor) {
        assertAll(
            () -> assertEquals("CF", creditor.getCreditorAccountType().getCreditorAccountType()),
            () -> assertEquals(77L, creditor.getCreditorAccountId()),
            () -> assertEquals("HM Courts & Tribunals Service", creditor.getMajorCreditorName())
        );
    }

    private void assertOffence(Offence offence) {
        assertAll(
            () -> assertEquals(33369L, offence.getOffenceId()),
            () -> assertEquals("HY35014", offence.getCjsCode()),
            () -> assertEquals("Riding a bicycle on a footpath", offence.getOffenceTitle())
        );
    }
}
