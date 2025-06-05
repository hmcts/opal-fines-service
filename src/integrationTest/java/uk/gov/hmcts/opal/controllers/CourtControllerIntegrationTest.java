package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.CourtControllerIntegrationTest")
@Sql(scripts = "classpath:db/insertData/insert_into_courts.sql", executionPhase = BEFORE_TEST_CLASS)
@DisplayName("CourtControllerIntegrationTest")
class CourtControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/courts";

    private static final String POST_COURTS_SEARCH_RESPONSE = "postCourtsSearchResponse.json";
    private static final String GET_COURTS_REF_DATA_RESPONSE = "getCourtsRefDataResponse.json";

    @MockBean
    UserStateService userStateService;

    @SpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    @Test
    @DisplayName("Get court by ID - When court does exist [@PO-272, @PO-424]")
    void testGetCourtById() throws Exception {

        ResultActions actions = mockMvc.perform(get(URL_BASE + "/7")
                                                    .header("authorization", "Bearer some_value"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetCourtById: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.courtId").value(7))
            .andExpect(jsonPath("$.businessUnitId").value(99))
            .andExpect(jsonPath("$.courtCode").value(7))
            .andExpect(jsonPath("$.name").value("AAA Test Court"))
            .andExpect(jsonPath("$.localJusticeAreaId").value(1013))
            .andExpect(jsonPath("$.nameCy").value(IsNull.nullValue()));

        // Currently no Schema to validate against
        // jsonSchemaValidationService.validateOrError(body, GET_COURTS_REF_DATA_RESPONSE);
    }


    @Test
    @DisplayName("Get court by ID - When court does not exist [@PO-272, @PO-424]")
    void testGetCourtById_WhenCourtDoesNotExist() throws Exception {

        ResultActions actions = mockMvc.perform(get(URL_BASE + "/2")
                                                    .header("authorization", "Bearer some_value"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetCourtById_WhenCourtDoesNotExist: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Post search courts - Should return matching court when one exists [@PO-272, @PO-424]")
    void testPostCourtsSearch() throws Exception {

        ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                                                    .header("authorization", "Bearer some_value")
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .content("{\"businessUnitId\":\"99\"}"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostCourtsSearch: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].courtId").value(7))
            .andExpect(jsonPath("$[0].businessUnitId").value(99))
            .andExpect(jsonPath("$[0].courtCode").value(7))
            .andExpect(jsonPath("$[0].name").value("AAA Test Court"))
            .andExpect(jsonPath("$[0].localJusticeAreaId").value(1013))
            .andExpect(jsonPath("$[0].nameCy").value(IsNull.nullValue()));

        // Currently no Schema to validate against
        // jsonSchemaValidationService.validateOrError(body, POST_COURTS_SEARCH_RESPONSE);
    }

    @Test
    @DisplayName("Post search courts - When court does not exist [@PO-272, @PO-424]")
    void testPostCourtsSearch_WhenCourtDoesNotExist() throws Exception {
        ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"courtId\":\"2\"}"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostCourtsSearch_WhenCourtDoesNotExist: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk());
    }

    @Test
    @DisplayName("Get court reference data - Should return court ref data when available [@PO-272, @PO-424]")
    void testGetCourtRefData() throws Exception {

        ResultActions actions = mockMvc.perform(get(URL_BASE)
                                                    .header("authorization", "Bearer some_value")
                                                    .param("business_unit", "99"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetCourtRefData: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[0].court_id").value(7))
            .andExpect(jsonPath("$.refData[0].court_code").value(7))
            .andExpect(jsonPath("$.refData[0].name").value("AAA Test Court"))
            .andExpect(jsonPath("$.refData[0].business_unit_id").value(99));

        jsonSchemaValidationService.validateOrError(body, GET_COURTS_REF_DATA_RESPONSE);
    }
}
