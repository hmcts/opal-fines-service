package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.OffenceControllerIntegrationTest")
@Sql(scripts = "classpath:db/insertData/insert_into_offences.sql", executionPhase = BEFORE_TEST_CLASS)
@DisplayName("OffenceController Integration Test")
class OffenceControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String GET_OFFENCES_REF_DATA_RESPONSE = "getOffencesRefDataResponse.json";
    private static final String POST_OFFENCES_SEARCH_RESPONSE = "postOffencesSearchResponse.json";
    private static final String URL_BASE = "/offences";

    @MockitoSpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    @Test
    @DisplayName("Get offence by ID [@PO-420, PO-272]")
    void testGetOffenceById() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE + "/30000"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetOffenceById: Response body:\n" + ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.offenceId").value(30000))
            .andExpect(jsonPath("$.cjsCode").value("AA60005"))
            .andExpect(jsonPath("$.offenceTitle")
                .value("Person having charge abandoning animal"))
            .andExpect(jsonPath("$.offenceTitleCy").doesNotExist());
    }


    @Test
    @DisplayName("Get offence reference data")
    void testGetOffenceReferenceData() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE).param("cjs_code","CW96023"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetOffenceReferenceData: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[0].offence_id").value(53115))
            .andExpect(jsonPath("$.refData[0].cjs_code").value("CW96023"))
            .andExpect(jsonPath("$.refData[0].offence_title")
                .value("Use a chemical weapon"));

        jsonSchemaValidationService.validateOrError(body, GET_OFFENCES_REF_DATA_RESPONSE);
    }


    @Test
    @DisplayName("Get no offences returned when offence does not exist [@PO-420, PO-272]")
    void testGetOffenceById_WhenOffenceDoesNotExist() throws Exception {
        mockMvc.perform(get(URL_BASE + "/999999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Post search result for offence created by POST request [@PO-926, PO-304]")
    void testPostOffencesSearch() throws Exception {
        ResultActions actions =  mockMvc.perform(post(URL_BASE + "/search")
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content("{\"cjs_code\":\"IC01001\"}"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostOffencesSearch: Response body:\n" + ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(4))
            .andExpect(jsonPath("$.searchData[0].offence_id").value(33430))
            .andExpect(jsonPath("$.searchData[0].cjs_code").value("IC01001"))
            .andExpect(jsonPath("$.searchData[0].offence_title").value("Genocide"))
            .andExpect(jsonPath("$.searchData[0].date_used_from").value("2001-09-01T00:00:00Z"))
            .andExpect(jsonPath("$.searchData[0].offence_oas")
                .value("Contrary to sections 51 and 53 of the International Criminal Court Act 2001."))
            .andReturn();

        jsonSchemaValidationService.validateOrError(body, POST_OFFENCES_SEARCH_RESPONSE);

    }

    @Test
    @DisplayName("Post no search result when offence does not exist [@PO-926, PO-304]")
    void testPostOffencesSearch_WhenOffenceDoesNotExist() throws Exception {
        ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"cjs_code\":\"NOTREALCODE\"}"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostOffencesSearch_WhenOffenceDoesNotExist: Response body:\n" + ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0));
    }

    @DisplayName("Get offence reference data by single cjs_code value [@PO-304, PO-1445]")
    @Test
    void testGetOffencesWithCjsCode() throws Exception {

        ResultActions actions = mockMvc.perform(get(URL_BASE)
                                                    .param("cjs_code", "CW96023"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetOffencesWithCjsCode: Response body:\n" + ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.refData[0].cjs_code").value("CW96023"))
            .andExpect(jsonPath("$.refData[0].offence_title")
                .value("Use a chemical weapon"))
            .andExpect(jsonPath("$.refData[0].offence_oas")
                .value("Contrary to section 2(1)(a) and (8) of the Chemical Weapons Act 1996."));
    }

    @Test
    @DisplayName("Get offences using comma-separated cjs_code values [@PO-304, PO-1445]")
    void testGetOffencesWithMultipleCjsCodes() throws Exception {

        ResultActions actions = mockMvc.perform(get(URL_BASE)
                .param("cjs_code","WT67003","ZP97010"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetOffencesWithMultipleCjsCodes: Response body:\n" + ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.refData", hasSize(2)))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.refData[*].cjs_code", containsInAnyOrder("WT67003","ZP97010")));
    }

    @Test
    @DisplayName("Post offence search with valid active_date in Zulu format [PO-1904]")
    void testPostOffencesSearchWithActiveDate() throws Exception {
        ResultActions actions = mockMvc.perform(post("/offences/search")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
            {
                "cjs_code": "IC01001",
                "active_date": "2001-09-01T00:00:00Z"
            }
            """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostOffencesSearchWithActiveDate: Response body:\n" + ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.searchData[0].offence_id").value(33430))
            .andExpect(jsonPath("$.searchData[0].cjs_code").value("IC01001"))
            .andExpect(jsonPath("$.searchData[0].offence_title").value("Genocide"));
    }
}
