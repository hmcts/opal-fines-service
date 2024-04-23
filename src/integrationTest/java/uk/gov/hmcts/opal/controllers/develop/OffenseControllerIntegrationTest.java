package uk.gov.hmcts.opal.controllers.develop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.opal.dto.search.OffenseSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.OffenseEntity;
import uk.gov.hmcts.opal.service.opal.OffenseService;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = OffenseController.class)
@ActiveProfiles({"integration"})
class OffenseControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    @Qualifier("offenseService")
    OffenseService offenseService;

    @Test
    void testGetOffenseById() throws Exception {
        OffenseEntity offenseEntity = createOffenseEntity();

        when(offenseService.getOffense((short)1)).thenReturn(offenseEntity);

        mockMvc.perform(get("/api/offense/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.offenseId").value(1))
            .andExpect(jsonPath("$.cjsCode").value("cjs-code"))
            .andExpect(jsonPath("$.offenseTitle").value("Title of Offense"))
            .andExpect(jsonPath("$.offenseTitleCy").value("Title of Offense CY"));
    }


    @Test
    void testGetOffenseById_WhenOffenseDoesNotExist() throws Exception {
        when(offenseService.getOffense((short)2)).thenReturn(null);

        mockMvc.perform(get("/api/offense/2"))
            .andExpect(status().isNoContent());
    }

    @Test
    void testPostOffensesSearch() throws Exception {
        OffenseEntity offenseEntity = createOffenseEntity();

        when(offenseService.searchOffenses(any(OffenseSearchDto.class))).thenReturn(singletonList(offenseEntity));

        mockMvc.perform(post("/api/offense/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"value\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].offenseId").value(1))
            .andExpect(jsonPath("$[0].cjsCode").value("cjs-code"))
            .andExpect(jsonPath("$[0].offenseTitle").value("Title of Offense"))
            .andExpect(jsonPath("$[0].offenseTitleCy").value("Title of Offense CY"));
    }

    @Test
    void testPostOffensesSearch_WhenOffenseDoesNotExist() throws Exception {
        // when(offenseService.getOffense(2L)).thenReturn(null);

        mockMvc.perform(post("/api/offense/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isNoContent());
    }

    private OffenseEntity createOffenseEntity() {
        return OffenseEntity.builder()
            .offenseId((short)1)
            .cjsCode("cjs-code")
            .businessUnit(BusinessUnitEntity.builder().build())
            .offenseTitle("Title of Offense")
            .offenseTitleCy("Title of Offense CY")
            .build();
    }
}
