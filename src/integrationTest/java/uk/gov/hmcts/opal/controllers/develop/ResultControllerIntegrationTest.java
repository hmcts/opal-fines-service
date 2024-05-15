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
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;
import uk.gov.hmcts.opal.entity.ResultEntity;
import uk.gov.hmcts.opal.service.opal.ResultService;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = ResultController.class)
@ActiveProfiles({"integration"})
class ResultControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    @Qualifier("resultService")
    ResultService resultService;

    @Test
    void testGetResultById() throws Exception {
        ResultEntity resultEntity = createResultEntity();

        when(resultService.getResult(1L)).thenReturn(resultEntity);

        mockMvc.perform(get("/api/result/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.resultId").value(1))
            .andExpect(jsonPath("$.resultTitle").value("Result AAA-BBB"))
            .andExpect(jsonPath("$.resultTitleCy").value("Result AAA-BBB Cy"))
            .andExpect(jsonPath("$.resultType").value("ResType-XX"))
            .andExpect(jsonPath("$.active").value(false))
            .andExpect(jsonPath("$.imposition").value(false))
            .andExpect(jsonPath("$.impositionCategory").value("Category of Imposition"))
            .andExpect(jsonPath("$.impositionAllocationPriority").value(9))
            .andExpect(jsonPath("$.impositionAccruing").value(false))
            .andExpect(jsonPath("$.impositionCreditor").value("AAA-01234"))
            .andExpect(jsonPath("$.enforcement").value(false))
            .andExpect(jsonPath("$.enforcementOverride").value(false))
            .andExpect(jsonPath("$.furtherEnforcementWarn").value(false))
            .andExpect(jsonPath("$.furtherEnforcementDisallow").value(false))
            .andExpect(jsonPath("$.enforcementHold").value(false))
            .andExpect(jsonPath("$.requiresEnforcer").value(false))
            .andExpect(jsonPath("$.generatesHearing").value(false))
            .andExpect(jsonPath("$.collectionOrder").value(false))
            .andExpect(jsonPath("$.extendTtpDisallow").value(false))
            .andExpect(jsonPath("$.extendTtpPreserveLastEnf").value(false))
            .andExpect(jsonPath("$.preventPaymentCard").value(false))
            .andExpect(jsonPath("$.listsMonies").value(false))
            .andExpect(jsonPath("$.userEntries").value("Entry from Users"));
    }


    @Test
    void testGetResultById_WhenResultDoesNotExist() throws Exception {
        when(resultService.getResult(2L)).thenReturn(null);

        mockMvc.perform(get("/api/result/2"))
            .andExpect(status().isNoContent());
    }

    @Test
    void testPostResultsSearch() throws Exception {
        ResultEntity resultEntity = createResultEntity();

        when(resultService.searchResults(any(ResultSearchDto.class))).thenReturn(singletonList(resultEntity));

        mockMvc.perform(post("/api/result/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"value\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].resultId").value(1))
            .andExpect(jsonPath("$[0].resultTitle").value("Result AAA-BBB"))
            .andExpect(jsonPath("$[0].resultTitleCy").value("Result AAA-BBB Cy"))
            .andExpect(jsonPath("$[0].resultType").value("ResType-XX"))
            .andExpect(jsonPath("$[0].active").value(false))
            .andExpect(jsonPath("$[0].imposition").value(false))
            .andExpect(jsonPath("$[0].impositionCategory").value("Category of Imposition"))
            .andExpect(jsonPath("$[0].impositionAllocationPriority").value(9))
            .andExpect(jsonPath("$[0].impositionAccruing").value(false))
            .andExpect(jsonPath("$[0].impositionCreditor").value("AAA-01234"))
            .andExpect(jsonPath("$[0].enforcement").value(false))
            .andExpect(jsonPath("$[0].enforcementOverride").value(false))
            .andExpect(jsonPath("$[0].furtherEnforcementWarn").value(false))
            .andExpect(jsonPath("$[0].furtherEnforcementDisallow").value(false))
            .andExpect(jsonPath("$[0].enforcementHold").value(false))
            .andExpect(jsonPath("$[0].requiresEnforcer").value(false))
            .andExpect(jsonPath("$[0].generatesHearing").value(false))
            .andExpect(jsonPath("$[0].collectionOrder").value(false))
            .andExpect(jsonPath("$[0].extendTtpDisallow").value(false))
            .andExpect(jsonPath("$[0].extendTtpPreserveLastEnf").value(false))
            .andExpect(jsonPath("$[0].preventPaymentCard").value(false))
            .andExpect(jsonPath("$[0].listsMonies").value(false))
            .andExpect(jsonPath("$[0].userEntries").value("Entry from Users"));
    }

    @Test
    void testPostResultsSearch_WhenResultDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/result/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isNoContent());
    }

    private ResultEntity createResultEntity() {
        return ResultEntity.builder()
            .resultId("1")
            .resultTitle("Result AAA-BBB")
            .resultTitleCy("Result AAA-BBB Cy")
            .resultType("ResType-XX")
            .active(false)
            .imposition(false)
            .impositionCategory("Category of Imposition")
            .impositionAllocationPriority((short)9)
            .impositionAccruing(false)
            .impositionCreditor("AAA-01234")
            .enforcement(false)
            .enforcementOverride(false)
            .furtherEnforcementWarn(false)
            .furtherEnforcementDisallow(false)
            .enforcementHold(false)
            .requiresEnforcer(false)
            .generatesHearing(false)
            .collectionOrder(false)
            .extendTtpDisallow(false)
            .extendTtpPreserveLastEnf(false)
            .preventPaymentCard(false)
            .listsMonies(false)
            .userEntries("Entry from Users")
            .build();
    }
}
