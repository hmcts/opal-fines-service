package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration"})
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1a=true",
    "launchdarkly.default-flag-values.release-1b=true",
    "launchdarkly.enabled=false"
})
@Slf4j(topic = "opal.ResultControllerIntegrationTest")
@Sql(scripts = "classpath:db/insertData/insert_into_results.sql", executionPhase = BEFORE_TEST_CLASS)
@DisplayName("ResultController Integration Test")
class ResultControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/results";
    private static final String GET_RESULTS_REF_DATA_RESPONSE =
        SchemaPaths.REFERENCE_DATA + "/getResultsRefDataResponse.json";

    @MockitoSpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    @Test
    @DisplayName("Get result by ID - validates all fields populated [@PO-703, PO-304, PO-2449]")
    @JiraStory("PO-703")
    @JiraStory("PO-304")
    @JiraStory("PO-2449")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6272")
    void testGetResultById() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE + "/BBBBBB"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetResultById_AllFieldsPopulated: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // Core fields
            .andExpect(jsonPath("$.result_id").value("BBBBBB"))
            .andExpect(jsonPath("$.result_title").value("Complete Result Entry for Testing"))
            .andExpect(jsonPath("$.result_title_cy").value("Cais Prawf Cyflawn"))
            .andExpect(jsonPath("$.result_type").value("Action"))
            .andExpect(jsonPath("$.active").value(true))
            // Imposition fields
            .andExpect(jsonPath("$.imposition_allocation_priority").value(5))
            .andExpect(jsonPath("$.imposition_creditor").value("CF"))
            .andExpect(jsonPath("$.imposition").value(true))
            .andExpect(jsonPath("$.imposition_category").value("Compensation"))
            .andExpect(jsonPath("$.imposition_accruing").value(true))
            // Enforcement fields
            .andExpect(jsonPath("$.enforcement").value(true))
            .andExpect(jsonPath("$.enforcement_override").value(true))
            .andExpect(jsonPath("$.further_enforcement_warn").value(true))
            .andExpect(jsonPath("$.further_enforcement_disallow").value(true))
            .andExpect(jsonPath("$.enforcement_hold").value(true))
            .andExpect(jsonPath("$.requires_enforcer").value(true))
            // Additional fields
            .andExpect(jsonPath("$.generates_hearing").value(true))
            .andExpect(jsonPath("$.collection_order").value(true))
            .andExpect(jsonPath("$.extend_ttp_disallow").value(true))
            .andExpect(jsonPath("$.extend_ttp_preserve_last_enf").value(true))
            .andExpect(jsonPath("$.prevent_payment_card").value(true))
            .andExpect(jsonPath("$.lists_monies").value(true))
            .andExpect(jsonPath("$.result_parameters").value("{\"param1\":\"value1\",\"param2\":\"value2\"}"))
            .andExpect(jsonPath("$.requires_employment_data").value(true))
            // New fields added for PO-7280
            .andExpect(jsonPath("$.allow_payment_terms").value(true))
            .andExpect(jsonPath("$.allow_additional_action").value(true))
            .andExpect(jsonPath("$.generates_warrant").value(true))
            .andExpect(jsonPath("$.requires_lja").value(true))
            .andExpect(jsonPath("$.manual_enforcement").value(false))
            .andExpect(jsonPath("$.enf_next_permitted_actions").value("NOENF,WDN"));
    }

    @Test
    @DisplayName("PO-2985 Get result by ID duplicates Welsh result parameters when requested")
    @JiraStory("PO-2985")
    @JiraEpic("PO-2630")
    void getResultById_whenIncludeWelshTrue_returnsWelshResultParameters() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE + "/DDDDDD?include_welsh=true"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":getResultById_whenIncludeWelshTrue_returnsWelshResultParameters: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.result_id").value("DDDDDD"))
            .andExpect(jsonPath("$.result_parameters").value(
                "[{\"name\":\"sample_name\",\"type\":\"text\",\"hint\":\"some hint\",\"language_dependent\":true},"
                    + "{\"name\":\"cy_sample_name\",\"type\":\"text\","
                    + "\"hint\":\"Provide a welsh version for the defendant\",\"language_dependent\":true},"
                    + "{\"name\":\"sample_name_2\",\"type\":\"text\",\"hint\":\"some hint 2\","
                    + "\"language_dependent\":false}]"));
    }

    @Test
    @DisplayName("No results returned when result does not exist [@PO-703, PO-304]")
    @JiraStory("PO-703")
    @JiraStory("PO-304")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6255")
    void testGetResultById_WhenResultDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/result/xyz"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get all results from endpoint [@PO-703, PO-304]")
    @JiraStory("PO-703")
    @JiraStory("PO-304")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6270")
    void testGetResultsRefData() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetAllResults: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(64))
            .andExpect(jsonPath("$.refData[?(@.result_id == 'AAAAAA')].result_title")
                           .value(hasItems("First Ever Result Entry for Testing")))
            .andExpect(jsonPath("$.refData[?(@.result_id == 'ABDC')].result_title")
                           .value(hasItems("Application made for Benefit Deductions")));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Get results by multiple IDs [@PO-703, PO-304]")
    @JiraStory("PO-703")
    @JiraStory("PO-304")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6254")
    void testGetResultsByIds() throws Exception {

        ResultActions actions = mockMvc.perform(get(URL_BASE + "?result_ids=AAAAAA,BWTD"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetResultsByIds: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.refData[?(@.result_id == 'AAAAAA')].result_title")
                           .value(hasItems("First Ever Result Entry for Testing")))
            .andExpect(jsonPath("$.refData[?(@.result_id == 'BWTD')].result_title")
                           .value(hasItems("Bail Warrant - dated")));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Get results by multiple IDs [@PO-703, PO-304]")
    @JiraStory("PO-703")
    @JiraStory("PO-304")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6271")
    void testGetResultsByIdsMultipleIds() throws Exception {

        ResultActions actions = mockMvc.perform(get(URL_BASE + "?result_ids=AAAAAA,DDDDDD"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetResultsByIds: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.refData[0].result_id").value("AAAAAA"))
            .andExpect(jsonPath("$.refData[0].result_title").value("First Ever Result Entry for Testing"))
            .andExpect(jsonPath("$.refData[1].result_id").value("DDDDDD"))
            .andExpect(jsonPath("$.refData[1].result_title").value("Bail Warrant - dated"));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Get result by single ID and validate result_parameters JSON")
    @JiraStory("PO-1771")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6257")
    void testGetSingleResultAndParameters() throws Exception {

        ResultActions actions = mockMvc.perform(get(URL_BASE + "?result_ids=DDDDDD"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetSingleResultAndParameters: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[0].result_id").value("DDDDDD"))
            .andExpect(jsonPath("$.refData[0].result_title").value("Bail Warrant - dated"))
            .andExpect(jsonPath("$.refData[0].result_title_cy").value("Gorchymyn Gollwng - dyddiedig"))
            .andExpect(jsonPath("$.refData[0].active").value(true))
            .andExpect(jsonPath("$.refData[0].result_type").value("Action"))
            .andExpect(jsonPath("$.refData[0].imposition_creditor").value(nullValue()))
            .andExpect(jsonPath("$.refData[0].imposition_allocation_order").value(nullValue()));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Get results by ids with unknown id returns only known results")
    @JiraStory("PO-1771")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6269")
    void testGetResultsWithUnknownId() throws Exception {

        ResultActions actions = mockMvc.perform(get(URL_BASE + "?result_ids=AAAAAA,UNKNOWN_ID"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetResultsWithUnknownId: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[0].result_id").value("AAAAAA"));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Get results by IDs with generates_hearing filter returns only those with generates_hearing=true")
    @JiraStory("PO-1771")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6261")
    void testGetResultsWithGeneratesHearingFilter() throws Exception {

        ResultActions actions = mockMvc.perform(get(URL_BASE
            + "?result_ids=AAAAAA,BBBBBB,DDDDDD&generates_hearing=true"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetResultsWithGeneratesHearingFilter: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.refData[*].result_id").value(hasItems("AAAAAA","BBBBBB")))
            .andExpect(jsonPath("$.refData[*].result_id").value(org.hamcrest.Matchers.not(hasItems("DDDDDD"))));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Get all active results when active=true")
    @JiraStory("PO-1771")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6275")
    void testGetResultsActiveFilter() throws Exception {

        ResultActions actions = mockMvc.perform(get(URL_BASE + "?active=true"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetResultsActiveFilter: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.refData[*].result_id").value(hasItems("AAAAAA","BBBBBB","DDDDDD")))
            .andExpect(jsonPath("$.refData[*].result_id").value(org.hamcrest.Matchers.not(hasItems("CC0000"))));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Generates hearing: generates_hearing=true returns those that generate hearings")
    @JiraStory("PO-1771")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6260")
    void testGeneratesHearingTrue() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE + "?generates_hearing=true"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGeneratesHearingTrue: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.refData[*].result_id").value(hasItems("AAAAAA","BBBBBB")))
            .andExpect(jsonPath("$.refData[*].result_id").value(
                org.hamcrest.Matchers.not(hasItems("DDDDDD","CC0000"))));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Generates hearing: generates_hearing=false returns those that do NOT generate hearings")
    @JiraStory("PO-1771")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6273")
    void testGeneratesHearingFalse() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE + "?generates_hearing=false"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGeneratesHearingFalse: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.refData[*].result_id").value(hasItems("DDDDDD","CC0000")))
            .andExpect(jsonPath("$.refData[*].result_id").value(
                org.hamcrest.Matchers.not(hasItems("AAAAAA","BBBBBB"))));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Generates hearing omitted (null) returns all")
    @JiraStory("PO-1771")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6259")
    void testGeneratesHearingNull() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGeneratesHearingNull: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.refData[*].result_id").value(hasItems("AAAAAA","BBBBBB","DDDDDD","CC0000")));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Manual enforcement: manual_enforcement_only=true returns those with manual_enforcement=true")
    @JiraStory("PO-1771")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6264")
    void testManualEnforcementTrue() throws Exception {
        // AAAAAA and DDDDDD have manual_enforcement = true
        ResultActions actions = mockMvc.perform(get(URL_BASE + "?manual_enforcement_only=true"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testManualEnforcementTrue: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.refData[*].result_id").value(hasItems("AAAAAA","DDDDDD")));
        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Manual enforcement: manual_enforcement_only=false returns those with manual_enforcement=false")
    @JiraStory("PO-1771")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6265")
    void testManualEnforcementFalse() throws Exception {
        // BBBBBB and CC0000 have manual_enforcement = false
        ResultActions actions = mockMvc.perform(get(URL_BASE + "?manual_enforcement_only=false"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testManualEnforcementFalse: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.refData[*].result_id").value(hasItems("BBBBBB","CC0000")));
        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Manual enforcement omitted returns all")
    @JiraStory("PO-1771")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6263")
    void testManualEnforcementNull() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testManualEnforcementNull: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.refData[*].result_id").value(hasItems("AAAAAA","BBBBBB","DDDDDD","CC0000")));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Enforcement: enforcement=true returns those with enforcement=true")
    @JiraStory("PO-1771")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6267")
    void testEnforcementTrue() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE + "?enforcement=true"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testEnforcementTrue: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.refData[*].result_id").value(hasItems("AAAAAA","BBBBBB")))
            .andExpect(jsonPath("$.refData[*].result_id").value(
                org.hamcrest.Matchers.not(hasItems("DDDDDD","CC0000"))));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Enforcement: enforcement=false returns those with enforcement=false")
    @JiraStory("PO-1771")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6258")
    void testEnforcementFalse() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE + "?enforcement=false"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testEnforcementFalse: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.refData[*].result_id").value(hasItems("DDDDDD","CC0000")))
            .andExpect(jsonPath("$.refData[*].result_id").value(
                org.hamcrest.Matchers.not(hasItems("AAAAAA","BBBBBB"))));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Enforcement omitted returns all")
    @JiraStory("PO-1771")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6266")
    void testEnforcementNull() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testEnforcementNull: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.refData[*].result_id").value(hasItems("AAAAAA","BBBBBB","DDDDDD","CC0000")));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Mixed booleans: active=true & enforcement=true returns intersection")
    @JiraStory("PO-1771")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6274")
    void testMixedActiveAndEnforcement() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE + "?active=true&enforcement=true"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testMixedActiveAndEnforcement: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.refData[*].result_id").value(hasItems("AAAAAA","BBBBBB")))
            .andExpect(jsonPath("$.refData[*].result_id").value(
                org.hamcrest.Matchers.not(hasItems("DDDDDD","CC0000"))));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Mixed booleans: active=true & manual_enforcement_only=true & enforcement=true "
        + "returns single AAAAAA from a constrained set")
    @JiraStory("PO-1771")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6262")
    void testMixedThreeBooleansReturnsSingle() throws Exception {
        // Within this constrained set, only AAAAAA satisfies all three filters.
        ResultActions actions = mockMvc.perform(get(URL_BASE
            + "?result_ids=AAAAAA,BBBBBB,DDDDDD&active=true&manual_enforcement_only=true&enforcement=true"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testMixedThreeBooleansReturnsSingle: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[0].result_id").value("AAAAAA"));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Get results with empty result_ids parameter returns all results")
    @JiraStory("PO-1771")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6268")
    void testGetResultsWithEmptyResultIdsReturnsAll() throws Exception {

        ResultActions actions = mockMvc.perform(get(URL_BASE + "?result_ids="));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetResultsWithEmptyResultIdsReturnsAll: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(64))
            // Assert that a few well-known IDs are present
            .andExpect(jsonPath("$.refData[*].result_id").value(hasItems(
                "AAAAAA",
                "BBBBBB",
                "DDDDDD",
                "CC0000",
                "BWTD"
            )));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Enforcement override: enforcement_override=true returns only those with enforcement_override=true")
    @JiraStory("PO-1852")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6256")
    void testEnforcementOverrideTrue() throws Exception {
        // Use real IDs: NBWT has enforcement_override = true, NAP has enforcement_override = false
        ResultActions actions = mockMvc.perform(get(URL_BASE + "?result_ids=NBWT,NAP&enforcement_override=true"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testEnforcementOverrideTrue: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // Expect only NBWT to be returned when enforcement_override=true
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[0].result_id").value("NBWT"))
            .andExpect(jsonPath("$.refData[*].result_id").value(org.hamcrest.Matchers.not(hasItems("NAP"))));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Enforcement override: enforcement_override=false returns only those with enforcement_override=false")
    @JiraStory("PO-1852")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-6253")
    void testEnforcementOverrideFalse() throws Exception {
        // Use real IDs: NBWT (true) and NAP (false) — expect only NAP when enforcement_override=false
        ResultActions actions = mockMvc.perform(get(URL_BASE + "?result_ids=NBWT,NAP&enforcement_override=false"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testEnforcementOverrideFalse: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // Expect only NAP to be returned when enforcement_override=false
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[0].result_id").value("NAP"))
            .andExpect(jsonPath("$.refData[*].result_id").value(org.hamcrest.Matchers.not(hasItems("NBWT"))));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

}
