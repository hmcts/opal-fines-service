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
class DefendantAccountSummarySchemasTests {

    @InjectMocks
    private JsonSchemaValidationService validator;

    private ObjectMapper mapper;
    private static final String GET_DEFENDANT_ACCOUNT_AT_A_GLANCE_RESPONSE_SCHEMA =
        SchemaPaths.DEFENDANT_ACCOUNT + "/getDefendantAccountAtAGlanceResponse.json";
    private static final String GET_DEFENDANT_ACCOUNT_HEADER_SUMMARY_RESPONSE_SCHEMA =
        SchemaPaths.DEFENDANT_ACCOUNT + "/getDefendantAccountHeaderSummaryResponse.json";
    private static final String UPDATE_DEFENDANT_ACCOUNT_REQUEST_SCHEMA =
        SchemaPaths.DEFENDANT_ACCOUNT + "/updateDefendantAccountRequest.json";
    private static final String ADD_NOTE_REQUEST_SCHEMA =
        SchemaPaths.DEFENDANT_ACCOUNT + "/addNoteRequest.json";
    private static final String ADD_NOTE_REQUEST_SCHEMA_LEGACY =
        "legacy/addNoteLegacyRequest.json";

    @BeforeEach
    public void setUp() {
        mapper = new ObjectMapper();
    }

    /*
    GET_DEFENDANT_ACCOUNT_AT_A_GLANCE_RESPONSE
    */

    @Test
    void testAAGValidJsonAgainstSchema() throws Exception {
        Map<String, Object> jsonMap = createValidAAGJson();
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, GET_DEFENDANT_ACCOUNT_AT_A_GLANCE_RESPONSE_SCHEMA);

        assertTrue(isValid, "Expected JSON to be valid against schema.");
    }

    @Test
    void testAAGMissingRequiredField() throws Exception {
        Map<String, Object> jsonMap = createValidAAGJson();
        jsonMap.remove("version"); // remove required field
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, GET_DEFENDANT_ACCOUNT_AT_A_GLANCE_RESPONSE_SCHEMA);

        assertFalse(isValid, "Expected validation to fail when a required field is missing.");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testAAGInvalidEnumValue() throws Exception {
        Map<String, Object> jsonMap = createValidAAGJson();
        Map<String, Object> debtorDetail = (Map<String, Object>) jsonMap.get("debtor_detail");
        debtorDetail.put("debtor_type", "InvalidType"); // invalid enum
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, GET_DEFENDANT_ACCOUNT_AT_A_GLANCE_RESPONSE_SCHEMA);

        assertFalse(isValid, "Expected validation to fail for invalid enum value.");
    }

    @Test
    void testAAGAdditionalPropertyNotAllowed() throws Exception {
        Map<String, Object> jsonMap = createValidAAGJson();
        jsonMap.put("unexpected_property", "should fail");
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, GET_DEFENDANT_ACCOUNT_AT_A_GLANCE_RESPONSE_SCHEMA);

        assertFalse(isValid, "Expected validation to fail due to additional property.");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testAAGOrganisationTrueWithMissingOrganisationName() throws Exception {
        Map<String, Object> jsonMap = createValidAAGJson();
        Map<String, Object> debtorDetail = (Map<String, Object>) jsonMap.get("debtor_detail");
        debtorDetail.put("organisation", true);
        debtorDetail.remove("organisation_name"); // omit required field when organisation=true

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        boolean isValid = validator.isValid(jsonNode, GET_DEFENDANT_ACCOUNT_AT_A_GLANCE_RESPONSE_SCHEMA);

        assertFalse(
            isValid,
            "Expected validation to fail when 'organisation' is true and 'organisation_name' is missing."
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void testAAGOrganisationTrueWithOrganisationNamePresent() throws Exception {
        Map<String, Object> jsonMap = createValidAAGJson();
        Map<String, Object> debtorDetail = (Map<String, Object>) jsonMap.get("debtor_detail");
        debtorDetail.put("organisation", true);
        debtorDetail.put("organisation_name", "Acme Corporation");

        // remove person-related fields which aren't required when organisation=true
        debtorDetail.replace("title", null);
        debtorDetail.replace("first_names", null);
        debtorDetail.replace("surname", null);
        debtorDetail.replace("date_of_birth", null);

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        boolean isValid = validator.isValid(jsonNode, GET_DEFENDANT_ACCOUNT_AT_A_GLANCE_RESPONSE_SCHEMA);

        assertTrue(
            isValid,
            "Expected validation to pass when 'organisation' is true and 'organisation_name' is present."
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void testAAGOrganisationFalseWithMissingPersonalDetails() throws Exception {
        Map<String, Object> jsonMap = createValidAAGJson();
        Map<String, Object> debtorDetail = (Map<String, Object>) jsonMap.get("debtor_detail");
        debtorDetail.put("organisation", false);
        // remove required person fields
        debtorDetail.remove("title");
        debtorDetail.remove("first_names");
        debtorDetail.remove("surname");
        debtorDetail.remove("date_of_birth");

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        boolean isValid = validator.isValid(jsonNode, GET_DEFENDANT_ACCOUNT_AT_A_GLANCE_RESPONSE_SCHEMA);

        assertFalse(
            isValid,
            "Expected validation to fail when 'organisation' is false and personal details are missing."
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void testAAGOrganisationFalseWithPersonalDetailsPresent() throws Exception {
        Map<String, Object> jsonMap = createValidAAGJson();
        Map<String, Object> debtorDetail = (Map<String, Object>) jsonMap.get("debtor_detail");
        debtorDetail.put("organisation", false);
        debtorDetail.put("title", "Ms");
        debtorDetail.put("first_names", "Jane");
        debtorDetail.put("surname", "Doe");
        debtorDetail.put("date_of_birth", "1985-05-15");

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        boolean isValid = validator.isValid(jsonNode, GET_DEFENDANT_ACCOUNT_AT_A_GLANCE_RESPONSE_SCHEMA);

        assertTrue(
            isValid,
            "Expected validation to pass when 'organisation' is false and personal details are present."
        );
    }

    /*
    GET_DEFENDANT_ACCOUNT_HEADER_SUMMARY_RESPONSE
    */
    @Test
    void testHeaderSummaryIndividualValidJsonAgainstSchema() throws Exception {
        Map<String, Object> jsonMap = createValidHeaderSummaryJson(false);
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, GET_DEFENDANT_ACCOUNT_HEADER_SUMMARY_RESPONSE_SCHEMA);

        assertTrue(isValid, "Expected JSON to be valid against schema.");
    }

    @Test
    void testHeaderSummaryOrganisationValidJsonAgainstSchema() throws Exception {
        Map<String, Object> jsonMap = createValidHeaderSummaryJson(true);
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, GET_DEFENDANT_ACCOUNT_HEADER_SUMMARY_RESPONSE_SCHEMA);

        assertTrue(isValid, "Expected JSON to be valid against schema.");
    }

    @Test
    void testHeaderSummaryInvalidDebtorTypeEnum() throws Exception {
        Map<String, Object> jsonMap = createValidHeaderSummaryJson(false);
        jsonMap.put("debtor_type", "InvalidType"); // invalid enum value
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        assertFalse(validator.isValid(jsonNode, GET_DEFENDANT_ACCOUNT_HEADER_SUMMARY_RESPONSE_SCHEMA));
    }

    @Test
    void testHeaderSummaryInvalidAccountStatusDisplayNameEnum() throws Exception {
        Map<String, Object> jsonMap = createValidHeaderSummaryJson(false);
        jsonMap.put("account_status_display_name", "InvalidStatus"); // invalid enum value
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, GET_DEFENDANT_ACCOUNT_HEADER_SUMMARY_RESPONSE_SCHEMA);
        assertFalse(isValid, "Expected validation to fail for invalid account_status_display_name enum value.");
    }

    @Test
    void testHeaderSummaryInvalidAccountTypeEnum() throws Exception {
        Map<String, Object> jsonMap = createValidHeaderSummaryJson(false);
        jsonMap.put("account_type", "InvalidType"); // invalid enum value
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, GET_DEFENDANT_ACCOUNT_HEADER_SUMMARY_RESPONSE_SCHEMA);
        assertFalse(isValid, "Expected validation to fail for invalid account_type enum value.");
    }

    @Test
    void testHeaderSummaryOrganisationConditionalFields() throws Exception {
        Map<String, Object> jsonMap = createValidHeaderSummaryJson(true);
        jsonMap.put("organisation_name", null);
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, GET_DEFENDANT_ACCOUNT_HEADER_SUMMARY_RESPONSE_SCHEMA);
        assertFalse(isValid, "Expected validation to fail when 'organisation_name' is null for organisation.");

    }

    /*
    UPDATE_DEFENDANT_ACCOUNT_REQUEST
    */
    @Test
    void testUpdateValidJsonAgainstSchema() throws Exception {
        Map<String, Object> jsonMap = createValidUpdateJson();
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, UPDATE_DEFENDANT_ACCOUNT_REQUEST_SCHEMA);

        assertTrue(isValid, "Expected JSON to be valid against schema.");
    }

    @Test
    void testUpdateInvalidJsonAgainstSchema() throws Exception {
        Map<String, Object> jsonMap = createValidUpdateJson();
        jsonMap.put("unexpected_property", "should fail"); // additional property
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, UPDATE_DEFENDANT_ACCOUNT_REQUEST_SCHEMA);

        assertFalse(isValid);
    }

    /*
    ADD_NOTE_REQUEST
    */
    @Test
    void testAddNoteValidJsonAgainstSchema() throws Exception {
        Map<String, Object> jsonMap = createValidAddNoteJson();
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, ADD_NOTE_REQUEST_SCHEMA);

        assertTrue(isValid, "Expected JSON to be valid against schema.");
    }

    @Test
    void testAddNoteInvalidEnumJsonAgainstSchema() throws Exception {
        Map<String, Object> jsonMap = createValidAddNoteJson();
        jsonMap.put("account_type", "InvalidType"); // invalid enum value
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, ADD_NOTE_REQUEST_SCHEMA);

        assertFalse(isValid, "Expected JSON to be valid against schema.");
    }

    @Test
    void testAddNoteInvalidConstJsonAgainstSchema() throws Exception {
        Map<String, Object> jsonMap = createValidAddNoteJson();
        jsonMap.put("note_type", "InvalidType"); // invalid constant value
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, ADD_NOTE_REQUEST_SCHEMA);

        assertFalse(isValid, "Expected JSON to be valid against schema.");
    }

    @Test
    void testAddNoteInvalidPropertyJsonAgainstSchema() throws Exception {
        Map<String, Object> jsonMap = createValidAddNoteJson();
        jsonMap.put("additional_property", "I shouldn't be here"); // invalid property
        JsonNode jsonNode = mapper.valueToTree(jsonMap);

        boolean isValid = validator.isValid(jsonNode, ADD_NOTE_REQUEST_SCHEMA);

        assertFalse(isValid, "Expected JSON to be valid against schema.");
    }

    @Test
    void testAddNoteValidJsonAgainstLegacySchema() throws Exception {
        Map<String, Object> jsonMap = createValidAddNoteJson();
        jsonMap.put("business_unit_id", "1");
        jsonMap.put("defendant_account_id", "1");
        String jsonString = mapper.writeValueAsString(jsonMap);

        boolean isValid = validator.isValid(jsonString, ADD_NOTE_REQUEST_SCHEMA_LEGACY);

        assertTrue(isValid, "Expected JSON to be valid against schema.");
    }

    @Test
    void testAddNoteMissingRequiredFieldJsonAgainstLegacySchema() throws Exception {
        Map<String, Object> jsonMap = createValidAddNoteJson();
        String jsonString = mapper.writeValueAsString(jsonMap);

        boolean isValid = validator.isValid(jsonString, ADD_NOTE_REQUEST_SCHEMA_LEGACY);

        assertFalse(isValid, "Expected JSON to be invalid against schema.");
    }

    private Map<String, Object> createValidAAGJson() {
        Map<String, Object> json = new HashMap<>();
        json.put("defendant_account_id", "12345");
        json.put("version", 1);
        json.put("account_number", "ACC123");

        Map<String, Object> debtorDetail = new HashMap<>();
        debtorDetail.put("debtor_type", "Defendant");
        debtorDetail.put("organisation", false);
        debtorDetail.put("address_line_1", "123 Main Street");
        debtorDetail.put("address_line_2", null);
        debtorDetail.put("address_line_3", null);
        debtorDetail.put("address_line_4", null);
        debtorDetail.put("address_line_5", null);
        debtorDetail.put("post_code", "AB12 3CD");
        debtorDetail.put("document_language", "EN");
        debtorDetail.put("hearing_language", "EN");
        debtorDetail.put("title", "Mr");
        debtorDetail.put("first_names", "John");
        debtorDetail.put("surname", "Doe");
        debtorDetail.put("date_of_birth", "1990-12-31");
        debtorDetail.put("national_insurance_number", "AB123456C");
        debtorDetail.put("individual_aliases", null);
        debtorDetail.put("organisation_name", null);
        debtorDetail.put("organisation_aliases", null);
        json.put("debtor_detail", debtorDetail);

        Map<String, Object> paymentTerms = new HashMap<>();
        paymentTerms.put("payment_terms_type_code", "B");
        paymentTerms.put("instalment_amount", null);
        paymentTerms.put("effective_date", null);
        paymentTerms.put("payment_terms_summary", null);
        paymentTerms.put("lump_sum_amount", null);
        paymentTerms.put("next_payment_date", null);
        paymentTerms.put("last_payment_date", null);
        paymentTerms.put("instalment_period", "W");
        json.put("payment_terms", paymentTerms);

        Map<String, Object> enforcementStatus = new HashMap<>();
        enforcementStatus.put("collection_order_made", true);
        enforcementStatus.put("last_enforcement_action", null);
        enforcementStatus.put("enforcement_override_id", null);
        enforcementStatus.put("last_movement_date", null);
        enforcementStatus.put("last_enforcement_action_title", null);
        enforcementStatus.put("default_days_in_jail", null);
        enforcementStatus.put("enforcement_override_title", null);
        json.put("enforcement_status", enforcementStatus);

        json.put("account_notes", null);

        return json;
    }


    private Map<String, Object> createValidHeaderSummaryJson(boolean isOrganisation) {
        Map<String, Object> json = new HashMap<>();

        // Required fields (common)
        json.put("defendant_account_id", "DEF12345");
        json.put("version", 1);
        json.put("account_number", "ACC98765");
        json.put("has_parent_guardian", false);
        json.put("debtor_type", "Defendant");
        json.put("organisation", isOrganisation);
        json.put("account_status_display_name", "Live");
        json.put("account_type", "Fine");
        json.put("prosecutor_case_reference", "CASE45678");
        json.put("business_unit_name", "Unit A");
        json.put("business_unit_id", "BU123");
        json.put("business_unit_code", "BUC456");
        json.put("imposed", 500.00);
        json.put("arrears", 100.00);
        json.put("paid", 400.00);
        json.put("written_off", 0.00);
        json.put("account_balance", 0.00);

        // Optional fields based on 'organisation' flag
        if (isOrganisation) {
            json.put("organisation_name", "Example Corp");
            json.put("is_youth", null);
            json.put("title", null);
            json.put("firstnames", null);
            json.put("surname", null);
        } else {
            json.put("is_youth", false);
            json.put("title", "Mr");
            json.put("firstnames", "John");
            json.put("surname", "Doe");
            json.put("organisation_name", null);
        }

        // Optional fields
        json.put("fixed_penalty_ticket_number", null);

        return json;
    }

    private Map<String, Object> createValidUpdateJson() {
        Map<String, Object> json = new HashMap<>();

        json.put("version", 1);
        json.put("account_comment", "Updated comment");
        json.put("account_free_note_1", "note1");
        json.put("account_free_note_2", "note2");
        json.put("account_free_note_3", "note3");

        return json;
    }

    private Map<String, Object> createValidAddNoteJson() {
        Map<String, Object> json = new HashMap<>();

        json.put("version", 1);
        json.put("account_type", "Defendant");
        json.put("account_id", "Acc1");
        json.put("account_note_text", "A note");
        json.put("note_type", "AA");

        return json;
    }
}
