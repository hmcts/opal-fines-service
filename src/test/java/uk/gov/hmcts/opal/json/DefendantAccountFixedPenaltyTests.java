package uk.gov.hmcts.opal.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DefendantAccountFixedPenaltyTests {

    @InjectMocks
    private JsonSchemaValidationService validator;

    private ObjectMapper mapper;
    private static final String GET_FIXED_PENAlTY_RESPONSE_SCHEMA =
        SchemaPaths.DEFENDANT_ACCOUNT + "/getDefendantAccountFixedPenaltyResponse.json";


    @BeforeEach
    public void setUp() {
        mapper = new ObjectMapper();
    }

    /*
     * GET_DEFENDANT_ACCOUNT_FIXED_PENALTY_RESPONSE
     */
    @Test
    void testGetNonVehicleFPValidJsonAgainstSchema() throws Exception {
        Map<String, Object> jsonMap = createValidGetFPJson();
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, GET_FIXED_PENAlTY_RESPONSE_SCHEMA);

        assertTrue(isValid, "Expected JSON to be valid against schema.");
    }

    @Test
    void testGetVehicleFPValidJsonAgainstSchema() throws Exception {
        Map<String, Object> jsonMap = createValidGetFPJson();
        jsonMap.put("vehicle_fixed_penalty_flag", true);
        jsonMap.put("vehicle_fixed_penalty_details", createValidVFPDetailsJson());
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, GET_FIXED_PENAlTY_RESPONSE_SCHEMA);

        assertTrue(isValid, "Expected JSON to be valid against schema.");
    }

    @Test
    void testGetNonVehicleFPInvalidJsonAgainstSchema() throws Exception {
        Map<String, Object> jsonMap = createValidGetFPJson();
        jsonMap.remove("fixed_penalty_ticket_details"); // Invalid: missing required field
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, GET_FIXED_PENAlTY_RESPONSE_SCHEMA);

        assertFalse(isValid, "Expected JSON to be invalid against schema.");
    }

    @Test
    void testGetVehicleFPInvalidJsonAgainstSchema() throws Exception {
        Map<String, Object> jsonMap = createValidGetFPJson();
        jsonMap.put("vehicle_fixed_penalty_flag", true);
        jsonMap.remove("vehicle_fixed_penalty_details"); // Invalid: missing required field
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, GET_FIXED_PENAlTY_RESPONSE_SCHEMA);

        assertFalse(isValid, "Expected JSON to be invalid against schema.");
    }

    /*
     * Helper JSON builders
     */
    private Map<String, Object> createValidGetFPJson() {
        Map<String, Object> json = new HashMap<>();
        json.put("version", 1);
        json.put("vehicle_fixed_penalty_flag", false);
        json.put("fixed_penalty_ticket_details", createValidFPTDetailsJson());
        json.put("vehicle_fixed_penalty_details", null);
        return json;
    }

    private Map<String, Object> createValidFPTDetailsJson() {
        Map<String, Object> json = new HashMap<>();
        json.put("issuing_authority", "Metropolitan Police Service");
        json.put("ticket_number", "FP123456");
        json.put("time_of_offence", "2023-10-01T12:00:00Z");
        json.put("place_of_offence", "London");
        return json;
    }

    private Map<String, Object> createValidVFPDetailsJson() {
        Map<String, Object> json = new HashMap<>();
        json.put("vehicle_registration_number", "AB12CDE");
        json.put("vehicle_drivers_license", "D1234567890");
        json.put("notice_number", "VN123456");
        json.put("date_notice_issued", "2023-10-01");
        return json;
    }
}
