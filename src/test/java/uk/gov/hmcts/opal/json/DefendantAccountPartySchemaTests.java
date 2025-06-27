
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DefendantAccountPartySchemaTests {

    @InjectMocks
    private JsonSchemaValidationService validator;

    private ObjectMapper mapper;
    private static final String GET_SCHEMA_FILE =
        SchemaPaths.DEFENDANT_ACCOUNT + "/getDefendantAccountPartyResponse.json";
    private static final String REPLACE_SCHEMA_FILE =
        SchemaPaths.DEFENDANT_ACCOUNT + "/replaceDefendantAccountPartyRequest.json";

    @BeforeEach
    public void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    void testDebtorTrueRequiresFields() throws Exception {
        Map<String, Object> jsonMap = createBaseJson();
        jsonMap.put("party_id", "123");
        jsonMap.put("debtor_flag", true);

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertTrue(validator.isValid(jsonNode, GET_SCHEMA_FILE));
    }

    @Test
    void testDebtorTrueMissingRequired() throws Exception {
        Map<String, Object> jsonMap = createBaseJson();
        jsonMap.put("party_id", "123");
        jsonMap.put("debtor_flag", true);
        jsonMap.put("employer_details", null);

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertFalse(validator.isValid(jsonNode, GET_SCHEMA_FILE));
    }

    @Test
    void testDebtorFalseOptionalFieldsNull() throws Exception {
        Map<String, Object> jsonMap = createBaseJson();
        jsonMap.put("party_id", "123");
        jsonMap.put("debtor_flag", false);
        jsonMap.put("contact_details", null);
        jsonMap.put("vehicle_details", null);
        jsonMap.put("language_preferences", null);
        jsonMap.put("employer_details", null);

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertTrue(validator.isValid(jsonNode, GET_SCHEMA_FILE));
    }

    @Test
    void testOrganisationTrueRequiresOrgFields() throws Exception {
        Map<String, Object> jsonMap = createBaseJson();
        jsonMap.put("party_id", "123");
        jsonMap.put("organisation_flag", true);
        Map<String, Object> partyDetails = new HashMap<>();
        partyDetails.put("organisation_name", "Test Org");
        partyDetails.put("organisation_aliases", new Object[] {});
        jsonMap.put("party_details", partyDetails);

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertTrue(validator.isValid(jsonNode, GET_SCHEMA_FILE));
    }

    @Test
    void testOrganisationFalseRequiresPersonalDetails() throws Exception {
        Map<String, Object> jsonMap = createBaseJson();
        jsonMap.put("party_id", "123");
        jsonMap.put("organisation_flag", false);

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertTrue(validator.isValid(jsonNode, GET_SCHEMA_FILE));
    }
    //TODO: Uncomment and implement this test when email validation is working
    /*
    @Test
    public void testInvalidEmailFormat() throws Exception {
        Map<String, Object> jsonMap = createBaseJson();
        Map<String, Object> contactDetails = new HashMap<>();
        contactDetails.put("primary_email_address", "not-an-email");
        jsonMap.put("contact_details", contactDetails);

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertFalse(validator.isValid(jsonNode, SCHEMA_FILE));
    }*/

    //REPLACE SCHEMA TESTS

    @Test
    void testReplaceDebtorTrueRequiresFields() throws Exception {
        Map<String, Object> jsonMap = createBaseJson();
        jsonMap.put("debtor_flag", true);

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertTrue(validator.isValid(jsonNode, REPLACE_SCHEMA_FILE));
    }

    @Test
    void testReplaceDebtorTrueMissingRequired() throws Exception {
        Map<String, Object> jsonMap = createBaseJson();
        jsonMap.put("debtor_flag", true);
        jsonMap.put("employer_details", null);

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertFalse(validator.isValid(jsonNode, REPLACE_SCHEMA_FILE));
    }

    @Test
    void testReplaceDebtorFalseOptionalFieldsNull() throws Exception {
        Map<String, Object> jsonMap = createBaseJson();
        jsonMap.put("debtor_flag", false);
        jsonMap.put("contact_details", null);
        jsonMap.put("vehicle_details", null);
        jsonMap.put("language_preferences", null);
        jsonMap.put("employer_details", null);

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertTrue(validator.isValid(jsonNode, REPLACE_SCHEMA_FILE));
    }

    @Test
    void testReplaceOrganisationTrueRequiresOrgFields() throws Exception {
        Map<String, Object> jsonMap = createBaseJson();
        jsonMap.put("organisation_flag", true);
        Map<String, Object> partyDetails = new HashMap<>();
        partyDetails.put("organisation_name", "Test Org");
        partyDetails.put("organisation_aliases", new Object[] {});
        jsonMap.put("party_details", partyDetails);

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertTrue(validator.isValid(jsonNode, REPLACE_SCHEMA_FILE));
    }

    @Test
    void testReplaceOrganisationFalseRequiresPersonalDetails() throws Exception {
        Map<String, Object> jsonMap = createBaseJson();
        jsonMap.put("organisation_flag", false);

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertTrue(validator.isValid(jsonNode, REPLACE_SCHEMA_FILE));
    }
    //TODO: Uncomment and implement this test when email validation is working
    /*
    @Test
    public void testInvalidEmailFormat() throws Exception {
        Map<String, Object> jsonMap = createBaseJson();
        Map<String, Object> contactDetails = new HashMap<>();
        contactDetails.put("primary_email_address", "not-an-email");
        jsonMap.put("contact_details", contactDetails);

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertFalse(validator.isValid(jsonNode, SCHEMA_FILE));
    }*/

    private Map<String, Object> createBaseJson() {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("version", 1);
        jsonMap.put("party_type", "Defendant");
        jsonMap.put("debtor_flag", true); // triggers nested required fields
        jsonMap.put("organisation_flag", false);

        // Party Details (individual)
        Map<String, Object> partyDetails = new HashMap<>();
        partyDetails.put("organisation_name", null);
        partyDetails.put("organisation_aliases", null);
        partyDetails.put("title", "Mr");
        partyDetails.put("first_names", "John");
        partyDetails.put("surname", "Doe");
        partyDetails.put("date_of_birth", "1980-01-01");
        partyDetails.put("age", "43");
        partyDetails.put("national_insurance_number", "AB123456C");

        Map<String, Object> alias = new HashMap<>();
        alias.put("alias_id", "alias-1");
        alias.put("sequence_number", 1);
        alias.put("surname", "Smith");
        alias.put("forenames", "Johnny");
        List<Map<String, Object>> individualAliases = new ArrayList<>();
        individualAliases.add(alias);
        partyDetails.put("individual_aliases", individualAliases);

        jsonMap.put("party_details", partyDetails);

        // Address
        Map<String, Object> address = new HashMap<>();
        address.put("address_line_1", "123 Main St");
        address.put("address_line_2", null);
        address.put("address_line_3", null);
        address.put("address_line_4", null);
        address.put("address_line_5", null);
        address.put("postcode", null);
        jsonMap.put("address", address);

        // Contact Details
        Map<String, Object> contactDetails = new HashMap<>();
        contactDetails.put("primary_email_address", "john.doe@example.com");
        contactDetails.put("secondary_email_address", null);
        contactDetails.put("mobile_telephone_number", "07123456789");
        contactDetails.put("home_telephone_number", null);
        contactDetails.put("work_telephone_number", null);
        jsonMap.put("contact_details", contactDetails);

        // Vehicle Details
        Map<String, Object> vehicleDetails = new HashMap<>();
        vehicleDetails.put("vehicle_make_and_model", null);
        vehicleDetails.put("vehicle_registration", null);
        jsonMap.put("vehicle_details", vehicleDetails);

        // Language Preferences
        Map<String, Object> languagePreferences = new HashMap<>();
        languagePreferences.put("document_language", "English");
        languagePreferences.put("court_hearing_language", null);
        jsonMap.put("language_preferences", languagePreferences);

        // Employer Details
        Map<String, Object> employerDetails = new HashMap<>();
        employerDetails.put("employer_name", "Example Corp");
        employerDetails.put("employer_reference", null);
        employerDetails.put("employer_email_address", null);
        employerDetails.put("employer_telephone_number", null);
        employerDetails.put("employer_address_line_1", "456 Corporate Way");
        employerDetails.put("employer_address_line_2", null);
        employerDetails.put("employer_address_line_3", null);
        employerDetails.put("employer_address_line_4", null);
        employerDetails.put("employer_address_line_5", null);
        employerDetails.put("employer_postcode", null);
        jsonMap.put("employer_details", employerDetails);

        return jsonMap;
    }

}
