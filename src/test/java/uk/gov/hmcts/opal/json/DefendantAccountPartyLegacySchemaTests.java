
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
class DefendantAccountPartyLegacySchemaTests {

    @InjectMocks
    private JsonSchemaValidationService validator;

    private ObjectMapper mapper;
    private static final String SCHEMA_FILE = "legacy/getDefendantAccountPartyLegacyResponse.json";

    @BeforeEach
    public void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    public void testDebtorTrueRequiresFields() throws Exception {
        Map<String, Object> jsonMap = createBaseJson();
        jsonMap.put("debtor_flag", true);
        jsonMap.put("contact_details", new HashMap<>());
        jsonMap.put("vehicle_details", new HashMap<>());
        jsonMap.put("language_preferences", new HashMap<>());
        jsonMap.put("employer_details", new HashMap<>());

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertTrue(validator.isValid(jsonNode, SCHEMA_FILE));
    }

    @Test
    public void testDebtorTrueMissingRequired() throws Exception {
        Map<String, Object> jsonMap = createBaseJson();
        jsonMap.put("debtor_flag", true);

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertFalse(validator.isValid(jsonNode, SCHEMA_FILE));
    }

    @Test
    public void testDebtorFalseOptionalFieldsNull() throws Exception {
        Map<String, Object> jsonMap = createBaseJson();
        jsonMap.put("debtor_flag", false);
        jsonMap.put("contact_details", null);
        jsonMap.put("vehicle_details", null);
        jsonMap.put("language_preferences", null);
        jsonMap.put("employer_details", null);

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertTrue(validator.isValid(jsonNode, SCHEMA_FILE));
    }

    @Test
    public void testOrganisationTrueRequiresOrgFields() throws Exception {
        Map<String, Object> jsonMap = createBaseJson();
        jsonMap.put("organisation_flag", true);
        Map<String, Object> partyDetails = new HashMap<>();
        partyDetails.put("organisation_name", "Test Org");
        partyDetails.put("organisation_aliases", new Object[] {});
        jsonMap.put("party_details", partyDetails);

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertTrue(validator.isValid(jsonNode, SCHEMA_FILE));
    }

    @Test
    public void testOrganisationFalseRequiresPersonalDetails() throws Exception {
        Map<String, Object> jsonMap = createBaseJson();
        jsonMap.put("organisation_flag", false);
        Map<String, Object> partyDetails = new HashMap<>();
        partyDetails.put("title", "Mr");
        partyDetails.put("first_names", "John");
        partyDetails.put("surname", "Doe");
        jsonMap.put("party_details", partyDetails);

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertTrue(validator.isValid(jsonNode, SCHEMA_FILE));
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
        jsonMap.put("party_id", "123");
        jsonMap.put("version", 1);
        jsonMap.put("party_type", "Defendant");
        jsonMap.put("debtor_flag", false);
        jsonMap.put("organisation_flag", false);

        Map<String, Object> partyDetails = new HashMap<>();
        partyDetails.put("title", "Mr");
        partyDetails.put("first_names", "John");
        partyDetails.put("surname", "Doe");
        jsonMap.put("party_details", partyDetails);

        Map<String, Object> address = new HashMap<>();
        address.put("address_line_1", "123 Main St");
        jsonMap.put("address", address);

        return jsonMap;
    }
}
