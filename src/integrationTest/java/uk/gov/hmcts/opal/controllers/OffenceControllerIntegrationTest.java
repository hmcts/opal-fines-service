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
import uk.gov.hmcts.opal.dto.search.OffenceSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.OffenceEntity;
import uk.gov.hmcts.opal.service.opal.OffenceService;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = OffenceController.class)
@ActiveProfiles({"integration"})
class OffenceControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    @Qualifier("offenceServiceProxy")
    OffenceService offenceService;

    @Test
    void testGetOffenceById() throws Exception {
        OffenceEntity offenceEntity = createOffenceEntity();

        when(offenceService.getOffence((short)1)).thenReturn(offenceEntity);

        mockMvc.perform(get("/api/offence/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.offenceId").value(1))
            .andExpect(jsonPath("$.cjsCode").value("cjs-code"))
            .andExpect(jsonPath("$.offenceTitle").value("Title of Offence"))
            .andExpect(jsonPath("$.offenceTitleCy").value("Title of Offence CY"));
    }


    @Test
    void testGetOffenceById_WhenOffenceDoesNotExist() throws Exception {
        when(offenceService.getOffence((short)2)).thenReturn(null);

        mockMvc.perform(get("/api/offence/2"))
            .andExpect(status().isNoContent());
    }

    @Test
    void testPostOffencesSearch() throws Exception {
        OffenceEntity offenceEntity = createOffenceEntity();

        when(offenceService.searchOffences(any(OffenceSearchDto.class))).thenReturn(singletonList(offenceEntity));

        mockMvc.perform(post("/api/offence/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"value\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].offenceId").value(1))
            .andExpect(jsonPath("$[0].cjsCode").value("cjs-code"))
            .andExpect(jsonPath("$[0].offenceTitle").value("Title of Offence"))
            .andExpect(jsonPath("$[0].offenceTitleCy").value("Title of Offence CY"));
    }

    @Test
    void testPostOffencesSearch_WhenOffenceDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/offence/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isNoContent());
    }

    private OffenceEntity createOffenceEntity() {
        return OffenceEntity.builder()
            .offenceId(1L)
            .cjsCode("cjs-code")
            .businessUnit(BusinessUnitEntity.builder().build())
            .offenceTitle("Title of Offence")
            .offenceTitleCy("Title of Offence CY")
            .build();
    }
}
