package uk.gov.hmcts.opal.controllers.till;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.opal.controllers.shared.util.OpenApiContractAssertions.assertJsonRequestMatchesBundledSpec;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

class TillsCreateSchemaIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ParameterizedTest
    @ValueSource(strings = {
        "{}",
        "{\"payer_type\":\"Individual\",\"payer_details\":{\"individual\":{}}}",
        "{\"payer_type\":\"Organisation\",\"payer_details\":{\"organisation\":{}}}",
        "{\"payment_reference\":\"ref-123\"}",
        "{\"payer_type\":\"Individual\",\"payer_details\":{\"individual\":{\"surname\":\"Smith\"}}}",
        """
            {"payer_type":"Individual","payer_details":{"individual":{"forenames":"Jane","surname":"Smith"},
             "address_line_1":"1 Street","address_line_2":"Town","address_line_3":"County","postcode":"AB1 2CD"}}
            """,
        """
            {"payer_type":"Organisation","payer_details":{"organisation":{"organisation_name":"Company"}}}
            """,
        """
            {"payer_type":"Organisation","payer_details":{"organisation":{"organisation_name":"Company"},
             "address_line_1":"1 Street","postcode":"AB1 2CD"}}
            """
    })
    void acceptsSupportedPayerShapes(String information) throws Exception {
        assertJsonRequestMatchesBundledSpec(objectMapper.readTree(request(information)),
            "/tills", "POST", "application/json");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{\"payer_type\":\"Individual\"}",
        "{\"payer_type\":\"Organisation\"}",
        "{\"payer_details\":{\"individual\":{\"surname\":\"Smith\"}}}",
        "{\"payer_type\":\"Individual\",\"payer_details\":{}}",
        "{\"payer_type\":\"Organisation\",\"payer_details\":{}}",
        "{\"payer_type\":\"Individual\",\"payer_details\":{\"organisation\":{\"organisation_name\":\"Company\"}}}",
        "{\"payer_type\":\"Organisation\",\"payer_details\":{\"individual\":{\"surname\":\"Smith\"}}}",
        """
            {"payer_type":"Individual","payer_details":{"individual":{"surname":"Smith"},
             "organisation":{"organisation_name":"Company"}}}
            """,
        """
            {"payer_type":"Organisation","payer_details":{"individual":{"surname":"Smith"},
             "organisation":{"organisation_name":"Company"}}}
            """,
        "{\"payer_type\":null}",
        "{\"payer_details\":null}"
    })
    void rejectsIncompleteMismatchedAndMixedShapes(String information) {
        assertThatThrownBy(() -> assertJsonRequestMatchesBundledSpec(objectMapper.readTree(request(information)),
            "/tills", "POST", "application/json"))
            .isInstanceOf(AssertionError.class).hasMessageContaining("OpenAPI schema validation failed");
    }

    private String request(String information) {
        ObjectNode request = (ObjectNode) objectMapper.readTree(
            """
                {"business_unit_id":10,"payments_in":[{"payment_details":{"amount":12.34,"method":"NC",
                 "destination_type":"S","allocation_type":"UN"},"additional_information":{
                 "payment_received_from":"D","suspense_reason_code":"UN","suspense_note":"note"}}]}
                """);
        ((ObjectNode) request.path("payments_in").get(0).path("additional_information"))
            .setAll((ObjectNode) objectMapper.readTree(information));
        return request.toString();
    }
}
