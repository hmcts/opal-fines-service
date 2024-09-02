package uk.gov.hmcts.opal.controllers;

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
import uk.gov.hmcts.opal.entity.CourtEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
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
class CourtControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    @Qualifier("courtServiceProxy")
    CourtService courtService;

    @MockBean
    UserStateService userStateService;

    @Test
    void testGetCourtById() throws Exception {
        CourtEntity courtEntity = CourtEntity.builder()
            .courtId(1L)
            .courtCode((short) 11)
            .nationalCourtCode("Test Court")
            .parentCourt(CourtEntity.builder().courtId(2L).build())
            .localJusticeArea(LocalJusticeAreaEntity.builder().localJusticeAreaId((short)22).build())
            .build();

        when(courtService.getCourt(1L)).thenReturn(courtEntity);

        mockMvc.perform(get("/api/court/1")
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
    void testGetCourtById_WhenCourtDoesNotExist() throws Exception {
        when(courtService.getCourt(2L)).thenReturn(null);

        mockMvc.perform(get("/api/court/2").header("authorization", "Bearer some_value"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testPostCourtsSearch() throws Exception {
        CourtEntity courtEntity = CourtEntity.builder()
            .courtId(1L)
            .courtCode((short) 11)
            .nationalCourtCode("Test Court")
            .parentCourt(CourtEntity.builder().courtId(2L).build())
            .localJusticeArea(LocalJusticeAreaEntity.builder().localJusticeAreaId((short)22).build())
            .build();

        when(courtService.searchCourts(any(CourtSearchDto.class))).thenReturn(singletonList(courtEntity));

        mockMvc.perform(post("/api/court/search")
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"value\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].courtId").value(1))
            .andExpect(jsonPath("$[0].courtCode").value(11))
            .andExpect(jsonPath("$[0].parentCourt.courtId").value(2))
            .andExpect(jsonPath("$[0].localJusticeArea.localJusticeAreaId").value(22))
            .andExpect(jsonPath("$[0].nationalCourtCode").value("Test Court"));
    }

    @Test
    void testPostCourtsSearch_WhenCourtDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/court/search")
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void testGetCourtRefData() throws Exception {
        CourtReferenceData refData = new CourtReferenceData(1L, (short)007, (short)11,
                                                            "Main Court", null, "MN1234");

        when(courtService.getReferenceData(any(), any())).thenReturn(singletonList(refData));

        mockMvc.perform(get("/api/court/ref-data")
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.refData[0].courtId").value(1))
            .andExpect(jsonPath("$.refData[0].courtCode").value(11))
            .andExpect(jsonPath("$.refData[0].name").value("Main Court"))
            .andExpect(jsonPath("$.refData[0].nationalCourtCode").value("MN1234"));
    }
}
