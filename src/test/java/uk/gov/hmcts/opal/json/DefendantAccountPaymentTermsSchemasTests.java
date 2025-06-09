package uk.gov.hmcts.opal.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DefendantAccountPaymentTermsSchemasTests {

    @InjectMocks
    private JsonSchemaValidationService validator;

    private ObjectMapper mapper;
    private static final String GET_PAYMENT_TERMS_RESPONSE_SCHEMA =
        "getDefendantAccountPaymentTermsResponse.json";
    private static final String ADD_PAYMENT_TERMS_REQUEST_SCHEMA =
        "addDefendantAccountPaymentTermsRequest.json";
    private static final String ADD_PAYMENT_CARD_REQUEST_SCHEMA =
        "addDefendantAccountPaymentCardRequestRequest.json";

    @BeforeEach
    public void setUp() {
        mapper = new ObjectMapper();
    }

    /*
     * GET_DEFENDANT_ACCOUNT_PAYMENT_TERMS_RESPONSE
     */
    @Test
    public void testGetPaymentTermsValidJsonAgainstSchema() throws Exception {
        Map<String, Object> jsonMap = createValidGetPTRJson();
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, GET_PAYMENT_TERMS_RESPONSE_SCHEMA);

        assertTrue(isValid, "Expected JSON to be valid against schema.");
    }

    @Test
    public void testGetPaymentTermsInvalidInstalmentPeriodJsonAgainstSchema() throws Exception {
        Map<String, Object> jsonMap = createValidGetPTRJson();
        jsonMap.put("instalment_period", "X"); // Invalid instalment period
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, GET_PAYMENT_TERMS_RESPONSE_SCHEMA);

        assertFalse(isValid, "Expected JSON to be invalid due to non enum value for instalment_period.");
    }

    @Test
    public void testGetPaymentTermsInvalidPaymentTermsJsonAgainstSchema() throws Exception {
        Map<String, Object> jsonMap = createValidGetPTRJson();
        jsonMap.put("payment_terms_type_code", "X"); // Invalid payment terms type code
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, GET_PAYMENT_TERMS_RESPONSE_SCHEMA);

        assertFalse(isValid, "Expected JSON to be invalid due to non enum value for payment_terms_type_code.");
    }

    @Test
    public void testGetPaymentTermsMissingRequiredField() throws Exception {
        Map<String, Object> jsonMap = createValidGetPTRJson();
        jsonMap.remove("version");
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, GET_PAYMENT_TERMS_RESPONSE_SCHEMA);

        assertFalse(isValid, "Expected JSON to be invalid due to missing 'version'.");
    }

    @Test
    public void testGetPaymentTermsInvalidTypeForInstalmentAmount() throws Exception {
        Map<String, Object> jsonMap = createValidGetPTRJson();
        jsonMap.put("instalment_amount", "invalid"); // Should be a number
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, GET_PAYMENT_TERMS_RESPONSE_SCHEMA);

        assertFalse(isValid, "Expected JSON to be invalid due to incorrect type for 'instalment_amount'.");
    }

    /*
     * ADD_DEFENDANT_ACCOUNT_PAYMENT_TERMS_REQUEST
     */
    @Test
    public void testAddPaymentTermsValidJsonAgainstSchema() throws Exception {
        Map<String, Object> jsonMap = createValidAddPTRJson();
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, ADD_PAYMENT_TERMS_REQUEST_SCHEMA);

        assertTrue(isValid, "Expected JSON to be valid against schema.");
    }

    @Test
    public void testAddPaymentTermsNullEnumValue() throws Exception {
        Map<String, Object> jsonMap = createValidAddPTRJson();
        jsonMap.put("payment_terms_type_code", null);
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, ADD_PAYMENT_TERMS_REQUEST_SCHEMA);

        assertFalse(isValid, "Expected JSON to be invalid due to null 'payment_terms_type_code'.");
    }

    @Test
    public void testAddPaymentTermsMissingEffectiveDate() throws Exception {
        Map<String, Object> jsonMap = createValidAddPTRJson();
        jsonMap.remove("effective_date");
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, ADD_PAYMENT_TERMS_REQUEST_SCHEMA);

        assertFalse(isValid, "Expected JSON to be invalid due to missing 'effective_date'.");
    }

    @Test
    public void testAddPaymentTermsInvalidEnumInstalmentPeriod() throws Exception {
        Map<String, Object> jsonMap = createValidAddPTRJson();
        jsonMap.put("instalment_period", "Z");
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, ADD_PAYMENT_TERMS_REQUEST_SCHEMA);

        assertFalse(isValid, "Expected JSON to be invalid due to invalid 'instalment_period'.");
    }

    /*
     * ADD_DEFENDANT_ACCOUNT_PAYMENT_CARD_REQUEST
     */
    @Test
    public void testAddPaymentCardRequestValidJsonAgainstSchema() throws Exception {
        Map<String, Object> jsonMap = createValidAddPCRJson();
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, ADD_PAYMENT_CARD_REQUEST_SCHEMA);

        assertTrue(isValid, "Expected JSON to be valid against schema.");
    }

    @Test
    public void testAddPaymentCardRequestMissingVersion() throws Exception {
        Map<String, Object> jsonMap = createValidAddPCRJson();
        jsonMap.remove("version");
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, ADD_PAYMENT_CARD_REQUEST_SCHEMA);

        assertFalse(isValid, "Expected JSON to be invalid due to missing 'version'.");
    }

    @Test
    public void testAddPaymentCardRequestInvalidVersionType() throws Exception {
        Map<String, Object> jsonMap = createValidAddPCRJson();
        jsonMap.put("version", "1"); // Should be integer
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, ADD_PAYMENT_CARD_REQUEST_SCHEMA);

        assertFalse(isValid, "Expected JSON to be invalid due to version being a string.");
    }

    /*
     * Helper JSON builders
     */
    private Map<String, Object> createValidGetPTRJson() {
        Map<String, Object> json = new HashMap<>();
        json.put("version", 1);
        json.put("payment_terms_type_code", "B");
        json.put("effective_date", "2023-10-01");
        json.put("instalment_period", "M");
        json.put("lump_sum", 100.00);
        json.put("instalment_amount", 100.00);
        json.put("days_in_default", 1);
        json.put("date_days_in_default_imposed", "2023-10-01");
        json.put("payment_card_last_requested", "2023-10-01");
        json.put("date_last_amended", "2023-10-01");
        json.put("last_amended_by", "testUser");
        json.put("last_amended_by_id", 12345);
        json.put("amendment_reason", "Initial terms");
        json.put("extension", false);
        json.put("last_enforcement", "2023-10-01");
        return json;
    }

    private Map<String, Object> createValidAddPTRJson() {
        Map<String, Object> json = new HashMap<>();
        json.put("version", 1);
        json.put("payment_terms_type_code", "B");
        json.put("effective_date", "2023-10-01");
        json.put("instalment_period", "M");
        json.put("lump_sum", 100.00);
        json.put("instalment_amount", 100.00);
        json.put("days_in_default", 1);
        json.put("date_days_in_default_imposed", "2023-10-01");
        json.put("reason", "Initial terms");
        json.put("last_enforcement", "2023-10-01");
        json.put("generate_payment_terms_change_letter", true);
        return json;
    }

    private Map<String, Object> createValidAddPCRJson() {
        Map<String, Object> json = new HashMap<>();
        json.put("version", 1);
        return json;
    }
}
