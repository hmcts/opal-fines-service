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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.LocalJusticeAreaControllerIntegrationTest")
@Sql(scripts = "classpath:db/insertData/insert_into_local_justice_area.sql", executionPhase = BEFORE_TEST_CLASS)
@DisplayName("LocalJusticeAreaController Integration Test")
class LocalJusticeAreaControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/local-justice-areas";
    private static final String GET_LJAS_REF_DATA_RESPONSE = "getLJARefDataResponse.json";

    @SpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    @Test
    @DisplayName("Get local justice area by ID [@PO-312, PO-304]")
    void testGetLocalJusticeAreaById() throws Exception {

        ResultActions actions = mockMvc.perform(get(URL_BASE + "/1"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetLocalJusticeAreaById: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.localJusticeAreaId").value(1))
            .andExpect(jsonPath("$.name").value("AAAA Trial Court"))
            .andExpect(jsonPath("$.addressLine1").value("Alpha Trial Courts"))
            .andExpect(jsonPath("$.addressLine2").value("Court Quarter"))
            .andExpect(jsonPath("$.addressLine3").value("666 Trial Street"))
            .andExpect(jsonPath("$.postcode").value("TR12 1TR"));
    }


    @Test
    @DisplayName("No local justice area returned when local justice area does not exist [@PO-312, PO-304]")
    void testGetLocalJusticeAreaById_WhenLocalJusticeAreaDoesNotExist() throws Exception {

        mockMvc.perform(get(URL_BASE + "/2"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Verify search result for local justice area created by POST request [@PO-312, PO-304]")
    void testPostLocalJusticeAreasSearch() throws Exception {

        ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"ljaCode\":\"00\"}"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostLocalJusticeAreasSearch: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].localJusticeAreaId").value(1))
            .andExpect(jsonPath("$[0].name").value("AAAA Trial Court"))
            .andExpect(jsonPath("$[0].addressLine1").value("Alpha Trial Courts"))
            .andExpect(jsonPath("$[0].addressLine2").value("Court Quarter"))
            .andExpect(jsonPath("$[0].addressLine3").value("666 Trial Street"))
            .andExpect(jsonPath("$[0].postcode").value("TR12 1TR"));
    }

    @Test
    void testGetLocalJusticeAreasRefData() throws Exception {

        ResultActions actions = mockMvc.perform(get(URL_BASE));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetLocalJusticeAreasRefData: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(648))
            .andExpect(jsonPath("$.refData[0].local_justice_area_id").value(1))
            .andExpect(jsonPath("$.refData[0].name").value("AAAA Trial Court"))
            .andExpect(jsonPath("$.refData[0].address_line_1").value("Alpha Trial Courts"))
            .andExpect(jsonPath("$.refData[0].lja_code").value("0007"));

        jsonSchemaValidationService.validateOrError(body, GET_LJAS_REF_DATA_RESPONSE);
    }

    @Test
    @DisplayName("Verify no search result when local justice area does not exist [@PO-312, PO-304]")
    void testPostLocalJusticeAreasSearch_WhenLocalJusticeAreaDoesNotExist() throws Exception {
        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isOk());
    }
}
