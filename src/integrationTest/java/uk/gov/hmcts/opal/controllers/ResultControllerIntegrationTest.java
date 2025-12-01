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
    @DisplayName("Get result by single ID and validate result_parameters JSON")
    void testGetSingleResultAndParameters() throws Exception {

        ResultActions actions = mockMvc.perform(get(URL_BASE + "?result_ids=BWTD"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetSingleResultAndParameters: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[0].result_id").value("BWTD"))
            .andExpect(jsonPath("$.refData[0].result_title").value("Bail Warrant - dated"))
            .andExpect(jsonPath("$.refData[0].result_title_cy").value("Gwarant Mechniaeth - Gyda dyddiad"))
            .andExpect(jsonPath("$.refData[0].active").value(true))
            .andExpect(jsonPath("$.refData[0].result_type").value("Result"))
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

        // AAAA has generates_hearing = true; BBBBBB has it = false
        ResultActions actions = mockMvc.perform(get(URL_BASE + "?result_ids=AAAAAA,BBBBBB&generates_hearing=true"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetResultsWithGeneratesHearingFilter: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.refData[0].result_id").value("AAAAAA"));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Get all active results when active=true")
    void testGetResultsActiveFilter() throws Exception {

        // CC0000 is inactive = false; others are active = true
        ResultActions actions = mockMvc.perform(get(URL_BASE + "?active=true"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetResultsActiveFilter: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // we expect 3 active rows: AAAA, BBBB, BWTD
            .andExpect(jsonPath("$.count").value(58))
            .andExpect(jsonPath("$.refData[*].result_id").value(hasItems("AAAAAA","BBBBBB","BWTD")))
            // ensure inactive one is not present
            .andExpect(jsonPath("$.refData[*].result_id").value(org.hamcrest.Matchers.not(hasItems("CC0000"))));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }
}

