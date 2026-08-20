package uk.gov.hmcts.opal.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.stream.Stream;

import static uk.gov.hmcts.opal.util.XmlUtil.XML_SCHEMA;

class LegacyVersionSchemaTest {

    private static final String OVER_LONG_MAX_VALUE = "9223372036854775808";
    private static final Path LEGACY_XSD_ROOT = Path.of("src/main/resources/xsd/legacy");
    private static final SchemaFactory SCHEMA_FACTORY = SchemaFactory.newInstance(XML_SCHEMA);

    @ParameterizedTest
    @MethodSource("versionResponses")
    void acceptsVersionValuesAboveLongMax(String schemaPath, String xml) throws IOException, SAXException {
        Schema schema = SCHEMA_FACTORY.newSchema(LEGACY_XSD_ROOT.resolve(schemaPath).toFile());
        Validator validator = schema.newValidator();

        validator.validate(new StreamSource(new StringReader(xml)));
    }

    private static Stream<Arguments> versionResponses() {
        return Stream.of(
            Arguments.of(
                "getDefendantAccountParty/get_defendant_account_party_response.xsd",
                partyResponse()
            ),
            Arguments.of(
                "replaceDefendantAccountParty/replace_defendant_account_party_response.xsd",
                partyResponse()
            ),
            Arguments.of(
                "removeDefendantAccountParty/remove_defendant_account_party_response.xsd",
                """
                    <response>
                      <version>%s</version>
                      <defendant_account_party_id>123</defendant_account_party_id>
                    </response>
                    """.formatted(OVER_LONG_MAX_VALUE)
            ),
            Arguments.of(
                "getMajorCreditorAccountAtAGlance/get_major_creditor_account_at_a_glance_response.xsd",
                """
                    <response>
                      <major_creditor>
                        <creditor_account_id>1</creditor_account_id>
                        <creditor_account_version>%s</creditor_account_version>
                        <name>Major Creditor</name>
                      </major_creditor>
                    </response>
                    """.formatted(OVER_LONG_MAX_VALUE)
            ),
            Arguments.of(
                "getMajorCreditorAccountHeaderSummary/get_major_creditor_account_header_summary_response.xsd",
                """
                    <response>
                      <major_creditor>
                        <creditor_account_id>1</creditor_account_id>
                        <account_version>%s</account_version>
                        <account_number>12345678</account_number>
                        <name>Major Creditor</name>
                        <account_reference>
                          <account_type>MJ</account_type>
                        </account_reference>
                      </major_creditor>
                      <business_unit_details>
                        <business_unit_name>Business Unit</business_unit_name>
                        <business_unit_id>77</business_unit_id>
                        <welsh_speaking>false</welsh_speaking>
                      </business_unit_details>
                      <awaiting_payout>0.00</awaiting_payout>
                    </response>
                    """.formatted(OVER_LONG_MAX_VALUE)
            )
        );
    }

    private static String partyResponse() {
        return """
            <response>
              <version>%s</version>
              <defendant_account_party>
                <defendant_account_party_type>Defendant</defendant_account_party_type>
                <is_debtor>true</is_debtor>
                <party_details>
                  <organisation_flag>false</organisation_flag>
                  <individual_details>
                    <surname>Smith</surname>
                  </individual_details>
                </party_details>
                <address>
                  <address_line_1>Address line 1</address_line_1>
                </address>
              </defendant_account_party>
            </response>
            """.formatted(OVER_LONG_MAX_VALUE);
    }
}
