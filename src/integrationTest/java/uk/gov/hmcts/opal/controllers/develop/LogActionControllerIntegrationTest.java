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
import uk.gov.hmcts.opal.dto.search.LogActionSearchDto;
import uk.gov.hmcts.opal.entity.LogActionEntity;
import uk.gov.hmcts.opal.service.opal.LogActionService;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = LogActionController.class)
@ActiveProfiles({"integration"})
class LogActionControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    @Qualifier("logActionService")
    LogActionService logActionService;

    @Test
    public void testGetLogActionById() throws Exception {
        LogActionEntity logActionEntity = createLogActionEntity();

        when(logActionService.getLogAction((short)1)).thenReturn(logActionEntity);

        mockMvc.perform(get("/api/log-action/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.logActionId").value(1))
            .andExpect(jsonPath("$.logActionName").value("Action Name"));
    }


    @Test
    public void testGetLogActionById_WhenLogActionDoesNotExist() throws Exception {
        when(logActionService.getLogAction((short)2)).thenReturn(null);

        mockMvc.perform(get("/api/log-action/2"))
            .andExpect(status().isNoContent());
    }

    @Test
    public void testPostLogActionsSearch() throws Exception {
        LogActionEntity logActionEntity = createLogActionEntity();

        when(logActionService.searchLogActions(any(LogActionSearchDto.class)))
            .thenReturn(singletonList(logActionEntity));

        mockMvc.perform(post("/api/log-action/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"value\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].logActionId").value(1))
            .andExpect(jsonPath("$[0].logActionName").value("Action Name"));
    }

    @Test
    public void testPostLogActionsSearch_WhenLogActionDoesNotExist() throws Exception {
        when(logActionService.getLogAction((short)2)).thenReturn(null);

        mockMvc.perform(post("/api/log-action/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isNoContent());
    }

    private LogActionEntity createLogActionEntity() {
        return LogActionEntity.builder()
            .logActionId((short)1)
            .logActionName("Action Name")
            .build();
    }
}
