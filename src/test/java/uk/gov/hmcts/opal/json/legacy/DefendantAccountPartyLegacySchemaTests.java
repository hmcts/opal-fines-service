package uk.gov.hmcts.opal.json.legacy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DefendantAccountPartyLegacySchemaTests {

    @InjectMocks
    private JsonSchemaValidationService validator;

    private ObjectMapper mapper;
    private static final String GET_SCHEMA_FILE = "legacy/getDefendantAccountPartyLegacyResponse.json";
    private static final String REPLACE_SCHEMA_FILE = "legacy/replaceDefendantAccountPartyLegacyRequest.json";

    @BeforeEach
    public void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    void testValidFullDefendantPayload() throws Exception {
        Map<String, Object> jsonMap = createGetDefendantAccountPartyLegacyResponseJson();
        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertTrue(validator.isValid(jsonNode, GET_SCHEMA_FILE));
    }

    @Test
    void testMissingRequiredVersion() throws Exception {
        Map<String, Object> jsonMap = createGetDefendantAccountPartyLegacyResponseJson();
        jsonMap.remove("version");
        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertFalse(validator.isValid(jsonNode, GET_SCHEMA_FILE));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testAliasWithInvalidSequenceNumber() throws Exception {
        Map<String, Object> alias = new HashMap<>();
        alias.put("alias_id", "BADSEQ");
        alias.put("sequence_number", 10); // invalid, should be 1–5
        alias.put("surname", "Bad");
        alias.put("forenames", "Alias");

        Map<String, Object> jsonMap = createGetDefendantAccountPartyLegacyResponseJson();
        Map<String, Object> defendantDetails = (Map<String, Object>) jsonMap.get("defendant_account_party");
        Map<String, Object> individualDetails = (Map<String, Object>) defendantDetails.get("individual_details");
        individualDetails.put("individual_aliases", List.of(alias));

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertFalse(validator.isValid(jsonNode, GET_SCHEMA_FILE));
    }

    @Test
    void testReplaceValidInput() throws Exception {
        Map<String, Object> jsonMap = createGetDefendantAccountPartyLegacyResponseJson();
        jsonMap.put("defendant_account_id", "456");
        jsonMap.put("business_unit_user_id", "123");
        jsonMap.put("business_unit_id", "789");
        jsonMap.put("defendant_account_party_id", "PARTY123456");

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertTrue(validator.isValid(jsonNode, REPLACE_SCHEMA_FILE));
    }

    @Test
    void testReplaceMissingBusinessUnitId() throws Exception {
        Map<String, Object> jsonMap = createGetDefendantAccountPartyLegacyResponseJson();
        jsonMap.put("defendant_account_id", "456");
        jsonMap.put("business_unit_user_id", "123");
        jsonMap.put("defendant_account_party_id", "PARTY123456");
        // Missing business_unit_id
        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertFalse(validator.isValid(jsonNode, REPLACE_SCHEMA_FILE));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testOnlyOrganisationDetailsAllowedWhenOrganisationFlagIsTrue() throws Exception {
        Map<String, Object> jsonMap = createGetDefendantAccountPartyLegacyResponseJson();
        Map<String, Object> defendantDetails = (Map<String, Object>) jsonMap.get("defendant_account_party");
        defendantDetails.put("organisation_flag", true);
        Map<String, Object> organisationDetails = new HashMap<>();
        organisationDetails.put("organisation_name", "Test Org");
        defendantDetails.put("organisation_details", organisationDetails);
        defendantDetails.remove("individual_details"); // Remove individual details

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertTrue(validator.isValid(jsonNode, GET_SCHEMA_FILE));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testBothIndividualAndOrganisationDetailsPresentFailsValidation() throws Exception {
        Map<String, Object> jsonMap = createGetDefendantAccountPartyLegacyResponseJson();
        Map<String, Object> defendantDetails = (Map<String, Object>) jsonMap.get("defendant_account_party");
        defendantDetails.put("organisation_flag", true);

        Map<String, Object> organisationDetails = new HashMap<>();
        organisationDetails.put("organisation_name", "Test Org");
        organisationDetails.put("company_registration_number", "12345678");
        defendantDetails.put("organisation_details", organisationDetails);

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertFalse(validator.isValid(jsonNode, GET_SCHEMA_FILE));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testNeitherIndividualNorOrganisationDetailsFailsValidation() throws Exception {
        Map<String, Object> jsonMap = createGetDefendantAccountPartyLegacyResponseJson();
        Map<String, Object> defendantDetails = (Map<String, Object>) jsonMap.get("defendant_account_party");
        defendantDetails.remove("individual_details");
        defendantDetails.remove("organisation_details");

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertFalse(validator.isValid(jsonNode, GET_SCHEMA_FILE));
    }

    private Map<String, Object> createGetDefendantAccountPartyLegacyResponseJson() {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("version", 1);

        Map<String, Object> partyDetails = new HashMap<>();
        partyDetails.put("party_id", "PARTY123456");
        partyDetails.put("organisation_flag", false);

        Map<String, Object> individualDetails = new HashMap<>();
        individualDetails.put("title", "Mr");
        individualDetails.put("first_names", "John Michael");
        individualDetails.put("surname", "Doe");
        individualDetails.put("date_of_birth", "1985-07-15");
        individualDetails.put("age", "39");
        individualDetails.put("national_insurance_number", "QQ123456C");

        Map<String, Object> alias = new HashMap<>();
        alias.put("alias_id", "ALIAS001");
        alias.put("sequence_number", 1);
        alias.put("surname", "Smith");
        alias.put("forenames", "Jonathan");
        individualDetails.put("individual_aliases", List.of(alias));

        partyDetails.put("individual_details", individualDetails);

        jsonMap.put("defendant_account_party", partyDetails);

        return jsonMap;
    }
}
