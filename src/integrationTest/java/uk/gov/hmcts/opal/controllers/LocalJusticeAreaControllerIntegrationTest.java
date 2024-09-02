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
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.service.opal.LocalJusticeAreaService;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = LocalJusticeAreaController.class)
@ActiveProfiles({"integration"})
class LocalJusticeAreaControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    @Qualifier("localJusticeAreaServiceProxy")
    LocalJusticeAreaService localJusticeAreaService;

    @Test
    void testGetLocalJusticeAreaById() throws Exception {
        LocalJusticeAreaEntity localJusticeAreaEntity = createLocalJusticeAreaEntity();

        when(localJusticeAreaService.getLocalJusticeArea((short)1)).thenReturn(localJusticeAreaEntity);

        mockMvc.perform(get("/api/local-justice-area/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.localJusticeAreaId").value(1))
            .andExpect(jsonPath("$.name").value("Local Justice Area 001"))
            .andExpect(jsonPath("$.addressLine1").value("Local Justice Street"))
            .andExpect(jsonPath("$.addressLine2").value("Local Justice Town"))
            .andExpect(jsonPath("$.addressLine3").value("Local Justice County"))
            .andExpect(jsonPath("$.postcode").value("LJ99 9LJ"));
    }


    @Test
    void testGetLocalJusticeAreaById_WhenLocalJusticeAreaDoesNotExist() throws Exception {
        when(localJusticeAreaService.getLocalJusticeArea((short)2)).thenReturn(null);

        mockMvc.perform(get("/api/local-justice-area/2"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testPostLocalJusticeAreasSearch() throws Exception {
        LocalJusticeAreaEntity localJusticeAreaEntity = createLocalJusticeAreaEntity();

        when(localJusticeAreaService.searchLocalJusticeAreas(any(LocalJusticeAreaSearchDto.class)))
            .thenReturn(singletonList(localJusticeAreaEntity));

        mockMvc.perform(post("/api/local-justice-area/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"value\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].localJusticeAreaId").value(1))
            .andExpect(jsonPath("$[0].name").value("Local Justice Area 001"))
            .andExpect(jsonPath("$[0].addressLine1").value("Local Justice Street"))
            .andExpect(jsonPath("$[0].addressLine2").value("Local Justice Town"))
            .andExpect(jsonPath("$[0].addressLine3").value("Local Justice County"))
            .andExpect(jsonPath("$[0].postcode").value("LJ99 9LJ"));
    }

    @Test
    void testPostLocalJusticeAreasSearch_WhenLocalJusticeAreaDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/local-justice-area/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isOk());
    }

    private LocalJusticeAreaEntity createLocalJusticeAreaEntity() {
        return LocalJusticeAreaEntity.builder()
            .localJusticeAreaId((short)1)
            .name("Local Justice Area 001")
            .addressLine1("Local Justice Street")
            .addressLine2("Local Justice Town")
            .addressLine3("Local Justice County")
            .postcode("LJ99 9LJ")
            .build();
    }
}
