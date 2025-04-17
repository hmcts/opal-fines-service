package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.projection.CourtReferenceData;
import uk.gov.hmcts.opal.service.opal.CourtService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import static java.util.Collections.singletonList;
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
@DisplayName("CourtController Integration Tests")
class CourtControllerIntegrationTest {

    private static final String URL_BASE = "/courts";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    @Qualifier("courtServiceProxy")
    CourtService courtService;

    @MockBean
    UserStateService userStateService;

    @Test
    @DisplayName("Get court by ID - When court does exist [@PO-272, @PO-424]")
    void testGetCourtById() throws Exception {
        CourtEntity.Lite courtEntity = CourtEntity.Lite.builder()
            .courtId(1L)
            .courtCode((short) 11)
            .nationalCourtCode("Test Court")
            .parentCourtId(2L)
            .localJusticeAreaId((short)22)
            .build();

        when(courtService.getCourtLite(1L)).thenReturn(courtEntity);

        mockMvc.perform(get(URL_BASE + "/1")
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.courtId").value(1))
            .andExpect(jsonPath("$.courtCode").value(11))
            .andExpect(jsonPath("$.parentCourtId").value(2))
            .andExpect(jsonPath("$.localJusticeAreaId").value(22))
            .andExpect(jsonPath("$.nationalCourtCode").value("Test Court"));
    }


    @Test
    @DisplayName("Get court by ID - When court does not exist [@PO-272, @PO-424]")
    void testGetCourtById_WhenCourtDoesNotExist() throws Exception {
        when(courtService.getCourtLite(2L)).thenReturn(null);

        mockMvc.perform(get(URL_BASE + "/2").header("authorization", "Bearer some_value"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Search courts - Should return matching court when one exists [@PO-272, @PO-424]")
    void testPostCourtsSearch() throws Exception {
        CourtEntity.Lite courtEntity = CourtEntity.Lite.builder()
            .courtId(1L)
            .courtCode((short) 11)
            .nationalCourtCode("Test Court")
            .parentCourtId(2L)
            .localJusticeAreaId((short)22)
            .build();

        when(courtService.searchCourts(any(CourtSearchDto.class))).thenReturn(singletonList(courtEntity));

        mockMvc.perform(post(URL_BASE + "/search")
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"value\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].courtId").value(1))
            .andExpect(jsonPath("$[0].courtCode").value(11))
            .andExpect(jsonPath("$[0].parentCourtId").value(2))
            .andExpect(jsonPath("$[0].localJusticeAreaId").value(22))
            .andExpect(jsonPath("$[0].nationalCourtCode").value("Test Court"));
    }

    @Test
    @DisplayName("Search courts - When court does not exist [@PO-272, @PO-424]")
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

        mockMvc.perform(get(URL_BASE)
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[0].court_id").value(1))
            .andExpect(jsonPath("$.refData[0].court_code").value(11))
            .andExpect(jsonPath("$.refData[0].name").value("Main Court"))
            .andExpect(jsonPath("$.refData[0].national_court_code").value("MN1234"));
    }
}
