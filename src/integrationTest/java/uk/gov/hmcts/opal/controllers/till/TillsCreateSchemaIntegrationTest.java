package uk.gov.hmcts.opal.controllers.till;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.opal.controllers.shared.util.OpenApiContractAssertions.assertJsonRequestMatchesBundledSpec;

import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

class TillsCreateSchemaIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonSchemaValidationService schemaValidationService = new JsonSchemaValidationService();

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
        assertBothSchemasAccept(objectMapper.readTree(request(information)));
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
        assertBothSchemasReject(objectMapper.readTree(request(information)));
    }

    @ParameterizedTest
    @JiraStory("PO-11365")
    @JiraEpic("PO-2439")
    @CsvSource({"F,D,", "F,T,", "C,A,", "C,T,", "S,D,OF", "S,A,FA", "S,T,MS", "S,D,UN"})
    void acceptsScenarioVariants(String destination, String receivedFrom, String reason) throws Exception {
        assertBothSchemasAccept(scenario(destination, receivedFrom, reason));
    }

    @ParameterizedTest
    @JiraStory("PO-11365")
    @JiraEpic("PO-2439")
    @CsvSource({"C,A,", "S,D,OF", "S,A,FA", "S,T,MS"})
    void rejectsMissingScenarioPayer(String destination, String receivedFrom, String reason) {
        ObjectNode request = scenario(destination, receivedFrom, reason);
        information(request).remove(List.of("payer_type", "payer_details"));
        assertBothSchemasReject(request);
    }

    @ParameterizedTest
    @JiraStory("PO-11365")
    @JiraEpic("PO-2439")
    @ValueSource(strings = {"Individual", "Organisation"})
    void rejectsStructuredCriminalPayer(String type) {
        ObjectNode request = scenario("F", "T", null);
        information(request).put("payer_type", type).putObject("payer_details")
            .putObject(type.equals("Individual") ? "individual" : "organisation");
        assertBothSchemasReject(request);
    }

    @ParameterizedTest
    @JiraStory("PO-11365")
    @JiraEpic("PO-2439")
    @ValueSource(strings = {"NC", "CQ", "CT", "PO"})
    void validatesTransferSourceForEveryMethod(String method) throws Exception {
        ObjectNode request = scenario("F", "D", null);
        ((ObjectNode) request.path("payments_in").get(0).path("payment_details")).put("method", method);
        if (method.equals("CT")) {
            assertBothSchemasReject(request);
        } else {
            assertBothSchemasAccept(request);
        }
        information(request).put("transfer_source", "NATWEST");
        assertBothSchemasAccept(request);
    }

    @ParameterizedTest
    @JiraStory("PO-11365")
    @JiraEpic("PO-2439")
    @CsvSource({
        "F,D,,defendant_account_id", "F,T,,third_party_payer_name", "F,D,,payment_received_from",
        "C,A,,court_fee_code", "C,A,,number_of_items", "C,A,,suspense_note",
        "S,D,OF,suspense_reason_code", "S,D,UN,suspense_note"
    })
    void rejectsMissingScenarioData(String destination, String receivedFrom, String reason, String field) {
        ObjectNode request = scenario(destination, receivedFrom, reason);
        if (field.equals("defendant_account_id")) {
            ((ObjectNode) request.path("payments_in").get(0)).remove(field);
        } else {
            information(request).remove(field);
        }
        assertBothSchemasReject(request);
    }

    @ParameterizedTest
    @JiraStory("PO-11365")
    @JiraEpic("PO-2439")
    @CsvSource({
        "F,D,,court_fee_code,FEE", "F,D,,suspense_note,Note",
        "C,A,,suspense_reason_code,UN", "C,A,,suspense_reason_detail,Detail",
        "S,D,OF,suspense_reason_detail,Detail", "S,D,FA,suspense_reason_detail,Detail",
        "S,D,MS,suspense_reason_detail,Detail", "S,D,UN,court_fee_code,FEE"
    })
    void rejectsFieldsFromOtherScenarios(String destination, String receivedFrom, String reason,
                                         String field, String value) {
        ObjectNode request = scenario(destination, receivedFrom, reason);
        information(request).put(field, value);
        assertBothSchemasReject(request);
    }

    private void assertBothSchemasAccept(JsonNode request) throws Exception {
        assertJsonRequestMatchesBundledSpec(request, "/tills", "POST", "application/json");
        schemaValidationService.validateOrError(request.toString(), "tills/createTillRequest.json");
    }

    private void assertBothSchemasReject(JsonNode request) {
        assertThatThrownBy(() -> assertJsonRequestMatchesBundledSpec(request, "/tills", "POST", "application/json"))
            .isInstanceOf(AssertionError.class).hasMessageContaining("OpenAPI schema validation failed");
        assertThatThrownBy(() -> schemaValidationService.validateOrError(request.toString(),
            "tills/createTillRequest.json"))
            .isInstanceOf(JsonSchemaValidationException.class);
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
