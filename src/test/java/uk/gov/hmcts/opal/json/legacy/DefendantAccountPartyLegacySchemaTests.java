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
    void testMissingRequiredPartyId() throws Exception {
        Map<String, Object> jsonMap = createGetDefendantAccountPartyLegacyResponseJson();
        jsonMap.remove("party_id");
        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertFalse(validator.isValid(jsonNode, GET_SCHEMA_FILE));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testInvalidEmailInContactDetails() throws Exception {
        Map<String, Object> jsonMap = createGetDefendantAccountPartyLegacyResponseJson();
        Map<String, Object> contactDetails = (Map<String, Object>) jsonMap.get("contact_details");
        contactDetails.put("primary_email_address", "invalid-email");
        jsonMap.put("contact_details", contactDetails);
        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertFalse(validator.isValid(jsonNode, GET_SCHEMA_FILE));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testInvalidDocumentLanguageCode() throws Exception {
        Map<String, Object> jsonMap = createGetDefendantAccountPartyLegacyResponseJson();
        Map<String, Object> defendantDetails = (Map<String, Object>) jsonMap.get("defendant_details");
        Map<String, Object> languagePrefs = (Map<String, Object>) defendantDetails.get("language_preferences");
        Map<String, Object> docLangPref = (Map<String, Object>) languagePrefs.get("document_language_preference");
        docLangPref.put("document_language_code", "FR"); // invalid enum
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
        Map<String, Object> defendantDetails = (Map<String, Object>) jsonMap.get("defendant_details");
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

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertTrue(validator.isValid(jsonNode, REPLACE_SCHEMA_FILE));
    }

    @Test
    void testReplaceMissingBusinessUnitId() throws Exception {
        Map<String, Object> jsonMap = createGetDefendantAccountPartyLegacyResponseJson();
        jsonMap.put("defendant_account_id", "456");
        jsonMap.put("business_unit_user_id", "123");
        // Missing business_unit_id
        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertFalse(validator.isValid(jsonNode, REPLACE_SCHEMA_FILE));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testOnlyOrganisationDetailsAllowedWhenOrganisationFlagIsTrue() throws Exception {
        Map<String, Object> jsonMap = createGetDefendantAccountPartyLegacyResponseJson();
        Map<String, Object> defendantDetails = (Map<String, Object>) jsonMap.get("defendant_details");
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
        Map<String, Object> defendantDetails = (Map<String, Object>) jsonMap.get("defendant_details");
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
        Map<String, Object> defendantDetails = (Map<String, Object>) jsonMap.get("defendant_details");
        defendantDetails.remove("individual_details");
        defendantDetails.remove("organisation_details");

        JsonNode jsonNode = mapper.valueToTree(jsonMap);
        assertFalse(validator.isValid(jsonNode, GET_SCHEMA_FILE));
    }

    private Map<String, Object> createGetDefendantAccountPartyLegacyResponseJson() {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("party_id", "PARTY123456");
        jsonMap.put("version", 1);

        Map<String, Object> defendantDetails = new HashMap<>();
        defendantDetails.put("debtor_type", "Defendant");
        defendantDetails.put("is_debtor", true);
        defendantDetails.put("organisation_flag", false);
        defendantDetails.put("is_youth_flag", false);

        Map<String, Object> address = new HashMap<>();
        address.put("address_line_1", "123 Justice Road");
        address.put("address_line_2", "Apt 4B");
        address.put("address_line_3", "Old Town");
        address.put("address_line_4", "Central District");
        address.put("address_line_5", "Wales");
        address.put("postcode", "WA1 2AB");
        defendantDetails.put("address", address);

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

        defendantDetails.put("individual_details", individualDetails);

        Map<String, Object> docLangPref = new HashMap<>();
        docLangPref.put("document_language_code", "EN");
        docLangPref.put("document_language_display_name", "English only");

        Map<String, Object> hearingLangPref = new HashMap<>();
        hearingLangPref.put("hearing_language_code", "EN");
        hearingLangPref.put("hearing_language_display_name", "English only");

        Map<String, Object> languagePrefs = new HashMap<>();
        languagePrefs.put("document_language_preference", docLangPref);
        languagePrefs.put("hearing_language_preference", hearingLangPref);
        defendantDetails.put("language_preferences", languagePrefs);

        jsonMap.put("defendant_details", defendantDetails);

        Map<String, Object> contactDetails = new HashMap<>();
        contactDetails.put("primary_email_address", "john.doe@example.com");
        contactDetails.put("secondary_email_address", "jm.doe@altmail.com");
        contactDetails.put("mobile_telephone_number", "07700900123");
        contactDetails.put("home_telephone_number", "02079460000");
        contactDetails.put("work_telephone_number", "02079460001");
        jsonMap.put("contact_details", contactDetails);

        Map<String, Object> vehicleDetails = new HashMap<>();
        vehicleDetails.put("vehicle_make_and_model", "Toyota Corolla");
        vehicleDetails.put("vehicle_registration", "AB12CDE");
        jsonMap.put("vehicle_details", vehicleDetails);

        Map<String, Object> employerDetails = new HashMap<>();
        employerDetails.put("employer_name", "TechCorp Ltd");
        employerDetails.put("employer_reference", "EMP-123456");
        employerDetails.put("employer_email_address", "hr@techcorp.com");
        employerDetails.put("employer_telephone_number", "03001234567");

        Map<String, Object> employerAddress = new HashMap<>();
        employerAddress.put("address_line_1", "456 Business Park");
        employerAddress.put("address_line_2", "Suite 200");
        employerAddress.put("address_line_3", "Commerce Way");
        employerAddress.put("address_line_4", "South Sector");
        employerAddress.put("address_line_5", "England");
        employerAddress.put("postcode", "EC1A 1BB");

        employerDetails.put("employer_address", employerAddress);
        jsonMap.put("employer_details", employerDetails);

        return jsonMap;
    }
}
