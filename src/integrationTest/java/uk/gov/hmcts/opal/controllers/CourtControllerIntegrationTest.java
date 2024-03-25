package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.CourtEntity;
import uk.gov.hmcts.opal.service.opal.CourtService;
import uk.gov.hmcts.opal.service.proxy.CourtServiceProxy;

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
    CourtService courtService;

    @MockBean
    CourtServiceProxy courtServiceProxy;

    @Test
    public void testGetCourtById() throws Exception {
        CourtEntity courtEntity = CourtEntity.builder()
            .courtId(1L)
            .courtCode((short) 11)
            .nationalCourtCode("Test Court")
            .parentCourtId(2L)
            .localJusticeAreaId((short) 22)
            .build();

        when(courtServiceProxy.getCourt(1L)).thenReturn(courtEntity);

        mockMvc.perform(get("/api/court/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.courtId").value(1))
            .andExpect(jsonPath("$.courtCode").value(11))
            .andExpect(jsonPath("$.parentCourtId").value(2))
            .andExpect(jsonPath("$.localJusticeAreaId").value(22))
            .andExpect(jsonPath("$.nationalCourtCode").value("Test Court"));
    }


    @Test
    public void testGetCourtById_WhenCourtDoesNotExist() throws Exception {
        when(courtServiceProxy.getCourt(2L)).thenReturn(null);

        mockMvc.perform(get("/api/court/2"))
            .andExpect(status().isNoContent());
    }

    @Test
    public void testPostCourtsSearch() throws Exception {
        CourtEntity courtEntity = CourtEntity.builder()
            .courtId(1L)
            .courtCode((short) 11)
            .nationalCourtCode("Test Court")
            .parentCourtId(2L)
            .localJusticeAreaId((short) 22)
            .build();

        when(courtService.searchCourts(any(CourtSearchDto.class))).thenReturn(singletonList(courtEntity));

        mockMvc.perform(post("/api/court/search")
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
    public void testPostCourtsSearch_WhenCourtDoesNotExist() throws Exception {
        when(courtService.getCourt(2L)).thenReturn(null);

        mockMvc.perform(post("/api/court/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isNoContent());
    }
}
