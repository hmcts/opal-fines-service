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
import uk.gov.hmcts.opal.dto.search.TillSearchDto;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.service.opal.TillService;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = TillController.class)
@ActiveProfiles({"integration"})
class TillControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    @Qualifier("tillService")
    TillService tillService;

    @Test
    public void testGetTillById() throws Exception {
        TillEntity tillEntity = createTillEntity();

        when(tillService.getTill(1L)).thenReturn(tillEntity);

        mockMvc.perform(get("/api/till/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.tillId").value(1))
            .andExpect(jsonPath("$.tillNumber").value(2))
            .andExpect(jsonPath("$.businessUnitId").value(3))
            .andExpect(jsonPath("$.ownedBy").value("Owner Keith"));
    }


    @Test
    public void testGetTillById_WhenTillDoesNotExist() throws Exception {
        when(tillService.getTill(2L)).thenReturn(null);

        mockMvc.perform(get("/api/till/2"))
            .andExpect(status().isNoContent());
    }

    @Test
    public void testPostTillsSearch() throws Exception {
        TillEntity tillEntity = createTillEntity();

        when(tillService.searchTills(any(TillSearchDto.class))).thenReturn(singletonList(tillEntity));

        mockMvc.perform(post("/api/till/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"value\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].tillId").value(1))
            .andExpect(jsonPath("$[0].tillNumber").value(2))
            .andExpect(jsonPath("$[0].businessUnitId").value(3))
            .andExpect(jsonPath("$[0].ownedBy").value("Owner Keith"));
    }

    @Test
    public void testPostTillsSearch_WhenTillDoesNotExist() throws Exception {
        when(tillService.getTill(2L)).thenReturn(null);

        mockMvc.perform(post("/api/till/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isNoContent());
    }

    private TillEntity createTillEntity() {
        return TillEntity.builder()
            .tillId(1L)
            .tillNumber((short)2)
            .businessUnitId((short)3)
            .ownedBy("Owner Keith")
            .build();
    }
}
