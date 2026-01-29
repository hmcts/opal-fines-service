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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

@ActiveProfiles({"integration"})
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
            .andExpect(jsonPath("$.result_parameters").value("{\"param1\":\"value1\",\"param2\":\"value2\"}"));
    }

    @Test
    @DisplayName("No results returned when result does not exist [@PO-703, PO-304]")
    void testGetResultById_WhenResultDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/result/xyz"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get all results from endpoint [@PO-703, PO-304]")
    void testGetResultsRefData() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetAllResults: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(64))
            .andExpect(jsonPath("$.refData[0].result_id").value("AAAAAA"))
            .andExpect(jsonPath("$.refData[0].result_title").value("First Ever Result Entry for Testing"))
            .andExpect(jsonPath("$.refData[1].result_id").value("ABDC"))
            .andExpect(jsonPath("$.refData[1].result_title").value("Application made for Benefit Deductions"));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Get results by multiple IDs [@PO-703, PO-304]")
    void testGetResultsByIds() throws Exception {

        ResultActions actions = mockMvc.perform(get(URL_BASE + "?result_ids=AAAAAA,BWTD"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetResultsByIds: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.refData[0].result_id").value("AAAAAA"))
            .andExpect(jsonPath("$.refData[0].result_title").value("First Ever Result Entry for Testing"))
            .andExpect(jsonPath("$.refData[1].result_id").value("BWTD"))
            .andExpect(jsonPath("$.refData[1].result_title").value("Bail Warrant - dated"));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Get results by multiple IDs [@PO-703, PO-304]")
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
    @DisplayName("Manual enforcement: manual_enforcement=true returns those with manual_enforcement=true")
    void testManualEnforcementTrue() throws Exception {
        // AAAAAA and DDDDDD have manual_enforcement = true
        ResultActions actions = mockMvc.perform(get(URL_BASE + "?manual_enforcement=true"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testManualEnforcementTrue: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.refData[*].result_id").value(hasItems("AAAAAA","DDDDDD")));
        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Manual enforcement: manual_enforcement=false returns those with manual_enforcement=false")
    void testManualEnforcementFalse() throws Exception {
        // BBBBBB and CC0000 have manual_enforcement = false
        ResultActions actions = mockMvc.perform(get(URL_BASE + "?manual_enforcement=false"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testManualEnforcementFalse: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.refData[*].result_id").value(hasItems("BBBBBB","CC0000")));
        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Manual enforcement omitted returns all")
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
    @DisplayName("Mixed booleans: active=true & manual_enforcement=true & enforcement=true returns single AAAAAA")
    void testMixedThreeBooleansReturnsSingle() throws Exception {
        // only AAAAAA satisfies all three: active=true, manual_enforcement=true and enforcement=true
        ResultActions actions = mockMvc.perform(get(URL_BASE
            + "?active=true&manual_enforcement=true&enforcement=true"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testMixedThreeBooleansReturnsSingle: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.refData[0].result_id").value("AAAAAA"));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Get results with empty result_ids parameter returns all results")
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
    void testEnforcementOverrideTrue() throws Exception {
        // Choose two ids where one (BBBBBB) has enforcement_override=true and the other (DDDDDD) does not.
        ResultActions actions = mockMvc.perform(get(URL_BASE + "?result_ids=BBBBBB,DDDDDD&enforcement_override=true"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testEnforcementOverrideTrue: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // Expect only BBBBBB to be returned when enforcement_override=true
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[0].result_id").value("BBBBBB"))
            .andExpect(jsonPath("$.refData[*].result_id").value(org.hamcrest.Matchers.not(hasItems("DDDDDD"))));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Enforcement override: enforcement_override=false returns only those with enforcement_override=false")
    void testEnforcementOverrideFalse() throws Exception {
        // Same pair of ids, but request enforcement_override=false; expect the other id (DDDDDD)
        ResultActions actions = mockMvc.perform(get(URL_BASE + "?result_ids=BBBBBB,DDDDDD&enforcement_override=false"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testEnforcementOverrideFalse: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // Expect only DDDDDD to be returned when enforcement_override=false
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[0].result_id").value("DDDDDD"))
            .andExpect(jsonPath("$.refData[*].result_id").value(org.hamcrest.Matchers.not(hasItems("BBBBBB"))));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

}

