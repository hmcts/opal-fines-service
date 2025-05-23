package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.CourtEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.projection.CourtReferenceData;
import uk.gov.hmcts.opal.service.opal.CourtService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = CourtController.class)
@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.CourtControllerIntegrationTest")
@DisplayName("CourtController Integration Tests")
class CourtControllerIntegrationTest {

    private static final String URL_BASE = "/courts";

    private static final String POST_COURTS_SEARCH_RESPONSE = "postCourtsSearchResponse.json";
    private static final String GET_COURTS_REF_DATA_RESPONSE = "getCourtsRefDataResponse.json";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    @Qualifier("courtServiceProxy")
    CourtService courtService;

    @MockBean
    UserStateService userStateService;

    @SpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    @Test
    @DisplayName("Get court by ID - When court does exist [@PO-272, @PO-424]")
    void testGetCourtById() throws Exception {
        CourtEntity courtEntity = CourtEntity.builder()
            .courtId(1L)
            .courtCode((short) 11)
            .nationalCourtCode("Test Court")
            .parentCourt(CourtEntity.builder().courtId(2L).build())
            .localJusticeArea(LocalJusticeAreaEntity.builder().localJusticeAreaId((short)22).build())
            .build();

        when(courtService.getCourt(1L)).thenReturn(courtEntity);

        mockMvc.perform(get(URL_BASE + "/1")
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.courtId").value(1))
            .andExpect(jsonPath("$.courtCode").value(11))
            .andExpect(jsonPath("$.parentCourt.courtId").value(2))
            .andExpect(jsonPath("$.localJusticeArea.localJusticeAreaId").value(22))
            .andExpect(jsonPath("$.nationalCourtCode").value("Test Court"));
    }


    @Test
    @DisplayName("Get court by ID - When court does not exist [@PO-272, @PO-424]")
    void testGetCourtById_WhenCourtDoesNotExist() throws Exception {
        when(courtService.getCourt(2L)).thenReturn(null);

        mockMvc.perform(get(URL_BASE + "/2").header("authorization", "Bearer some_value"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Post search courts - Should return matching court when one exists [@PO-272, @PO-424]")
    void testPostCourtsSearch() throws Exception {
        CourtEntity courtEntity = CourtEntity.builder()
            .courtId(1L)
            .courtCode((short) 11)
            .nationalCourtCode("Test Court")
            .parentCourt(CourtEntity.builder().courtId(2L).build())
            .localJusticeArea(LocalJusticeAreaEntity.builder().localJusticeAreaId((short)22).build())
            .build();

        when(courtService.searchCourts(any(CourtSearchDto.class))).thenReturn(singletonList(courtEntity));

        ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"value\"}"));
        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostCourtsSearch: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].courtId").value(1))
            .andExpect(jsonPath("$[0].courtCode").value(11))
            .andExpect(jsonPath("$[0].parentCourt.courtId").value(2))
            .andExpect(jsonPath("$[0].localJusticeArea.localJusticeAreaId").value(22))
            .andExpect(jsonPath("$[0].nationalCourtCode").value("Test Court"));

        // TODO Schema and call response both need further refinement
        // assertTrue(jsonSchemaValidationService.isValid(body, POST_COURTS_SEARCH_RESPONSE));
    }

    @Test
    @DisplayName("Post search courts - When court does not exist [@PO-272, @PO-424]")
    void testPostCourtsSearch_WhenCourtDoesNotExist() throws Exception {
        mockMvc.perform(post(URL_BASE + "/search")
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Get court reference data - Should return court ref data when available [@PO-272, @PO-424]")
    void testGetCourtRefData() throws Exception {
        CourtReferenceData refData = new CourtReferenceData(1L, (short)007, (short)11,
                                                            "Main Court", null, "MN1234");

        when(courtService.getReferenceData(any(), any())).thenReturn(singletonList(refData));

        ResultActions actions = mockMvc.perform(get(URL_BASE)
                            .header("authorization", "Bearer some_value"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetCourtRefData: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[0].court_id").value(1))
            .andExpect(jsonPath("$.refData[0].court_code").value(11))
            .andExpect(jsonPath("$.refData[0].name").value("Main Court"))
            .andExpect(jsonPath("$.refData[0].national_court_code").value("MN1234"));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_COURTS_REF_DATA_RESPONSE));
    }
}
