package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.ResultControllerIntegrationTest")
@Sql(scripts = "classpath:db/insertData/insert_into_results.sql", executionPhase = BEFORE_TEST_CLASS)
@DisplayName("ResultController Integration Test")
class ResultControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/results";
    private static final String GET_RESULTS_REF_DATA_RESPONSE = "getResultsRefDataResponse.json";

    @SpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    @Test
    @DisplayName("Get result by ID [@PO-703, PO-304]")
    void testGetResultById() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE + "/AAAAAA"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetResultById: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.result_id").value("AAAAAA"))
            .andExpect(jsonPath("$.result_title").value("First Ever Result Entry for Testing"));
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
            .andExpect(jsonPath("$.count").value(61))
            .andExpect(jsonPath("$.refData[0].result_id").value("AAAAAA"))
            .andExpect(jsonPath("$.refData[0].result_title").value("First Ever Result Entry for Testing"))
            .andExpect(jsonPath("$.refData[1].result_id").value("ABDC"))
            .andExpect(jsonPath("$.refData[1].result_title").value("Application made for benefit deductions"));

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
            .andExpect(jsonPath("$.refData[1].result_title").value("Bail Warrant - Dated"));

        jsonSchemaValidationService.validateOrError(body, GET_RESULTS_REF_DATA_RESPONSE);
    }
}
