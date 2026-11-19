package uk.gov.hmcts.opal.controllers.till;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.opal.controllers.shared.util.OpenApiContractAssertions.assertJsonRequestMatchesBundledSpec;

import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

class TillsCreateSchemaIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ParameterizedTest
    @JiraStory("PO-11365")
    @JiraEpic("PO-2439")
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
    @JiraStory("PO-11365")
    @JiraEpic("PO-2439")
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

    @ParameterizedTest
    @JiraStory("PO-11365")
    @JiraEpic("PO-2439")
    @CsvSource({"F,D,", "F,T,", "C,A,", "C,T,", "S,D,OF", "S,A,FA", "S,T,MS", "S,D,UN"})
    void acceptsScenarioVariants(String destination, String receivedFrom, String reason) throws Exception {
        assertJsonRequestMatchesBundledSpec(scenario(destination, receivedFrom, reason),
            "/tills", "POST", "application/json");
    }

    @ParameterizedTest
    @JiraStory("PO-11365")
    @JiraEpic("PO-2439")
    @CsvSource({"C,A,", "S,D,OF", "S,A,FA", "S,T,MS"})
    void rejectsMissingScenarioPayer(String destination, String receivedFrom, String reason) {
        ObjectNode request = scenario(destination, receivedFrom, reason);
        information(request).remove(List.of("payer_type", "payer_details"));
        assertThatThrownBy(() -> assertJsonRequestMatchesBundledSpec(request, "/tills", "POST", "application/json"))
            .isInstanceOf(AssertionError.class).hasMessageContaining("OpenAPI schema validation failed");
    }

    @ParameterizedTest
    @JiraStory("PO-11365")
    @JiraEpic("PO-2439")
    @ValueSource(strings = {"Individual", "Organisation"})
    void rejectsStructuredCriminalPayer(String type) {
        ObjectNode request = scenario("F", "T", null);
        information(request).put("payer_type", type).putObject("payer_details")
            .putObject(type.equals("Individual") ? "individual" : "organisation");
        assertThatThrownBy(() -> assertJsonRequestMatchesBundledSpec(request, "/tills", "POST", "application/json"))
            .isInstanceOf(AssertionError.class).hasMessageContaining("OpenAPI schema validation failed");
    }

    private ObjectNode scenario(String destination, String receivedFrom, String reason) {
        ObjectNode request = (ObjectNode) objectMapper.readTree(request("{}"));
        ObjectNode payment = (ObjectNode) request.path("payments_in").get(0);
        ((ObjectNode) payment.path("payment_details")).put("destination_type", destination);
        ObjectNode information = information(request);
        information.put("payment_received_from", receivedFrom);
        if (!destination.equals("S")) {
            information.remove("suspense_reason_code");
        } else {
            information.put("suspense_reason_code", reason);
        }
        if (destination.equals("F")) {
            payment.put("defendant_account_id", 99L);
            information.remove("suspense_note");
            if (receivedFrom.equals("T")) {
                information.put("third_party_payer_name", "Third party");
            }
        } else if (!"UN".equals(reason)) {
            information.put("payer_type", "Individual").putObject("payer_details").putObject("individual");
        }
        if (destination.equals("C")) {
            information.put("court_fee_code", "FEE").put("number_of_items", 1);
        }
        return request;
    }

    private static ObjectNode information(ObjectNode request) {
        return (ObjectNode) request.path("payments_in").get(0).path("additional_information");
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
