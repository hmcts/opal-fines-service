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
import uk.gov.hmcts.opal.dto.search.LogAuditDetailSearchDto;
import uk.gov.hmcts.opal.entity.LogAuditDetailEntity;
import uk.gov.hmcts.opal.service.opal.LogAuditDetailService;

import java.time.LocalDateTime;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = LogAuditDetailController.class)
@ActiveProfiles({"integration"})
class LogAuditDetailControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    @Qualifier("logAuditDetailService")
    LogAuditDetailService logAuditDetailService;

    @Test
    public void testGetLogAuditDetailById() throws Exception {
        LogAuditDetailEntity logAuditDetailEntity = createLogAuditDetailEntity();

        when(logAuditDetailService.getLogAuditDetail(1L)).thenReturn(logAuditDetailEntity);

        mockMvc.perform(get("/api/log-audit-detail/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.logAuditDetailId").value(1))
            .andExpect(jsonPath("$.userId").value(4))
            .andExpect(jsonPath("$.logActionId").value(2))
            .andExpect(jsonPath("$.accountNumber").value("1234ACC"))
            .andExpect(jsonPath("$.businessUnitId").value(3))
            .andExpect(jsonPath("$.jsonRequest").value("{\"request\":\"processAudit\"}"));
    }


    @Test
    public void testGetLogAuditDetailById_WhenLogAuditDetailDoesNotExist() throws Exception {
        when(logAuditDetailService.getLogAuditDetail(2L)).thenReturn(null);

        mockMvc.perform(get("/api/log-audit-detail/2"))
            .andExpect(status().isNoContent());
    }

    @Test
    public void testPostLogAuditDetailsSearch() throws Exception {
        LogAuditDetailEntity logAuditDetailEntity = createLogAuditDetailEntity();

        when(logAuditDetailService.searchLogAuditDetails(any(LogAuditDetailSearchDto.class)))
            .thenReturn(singletonList(logAuditDetailEntity));

        mockMvc.perform(post("/api/log-audit-detail/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"value\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].logAuditDetailId").value(1))
            .andExpect(jsonPath("$[0].userId").value(4))
            .andExpect(jsonPath("$[0].logActionId").value(2))
            .andExpect(jsonPath("$[0].accountNumber").value("1234ACC"))
            .andExpect(jsonPath("$[0].businessUnitId").value(3))
            .andExpect(jsonPath("$[0].jsonRequest").value("{\"request\":\"processAudit\"}"));
    }

    @Test
    public void testPostLogAuditDetailsSearch_WhenLogAuditDetailDoesNotExist() throws Exception {
        when(logAuditDetailService.getLogAuditDetail(2L)).thenReturn(null);

        mockMvc.perform(post("/api/log-audit-detail/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isNoContent());
    }

    private LogAuditDetailEntity createLogAuditDetailEntity() {
        return LogAuditDetailEntity.builder()
            .logAuditDetailId(1L)
            .userId(4L)
            .logTimestamp(LocalDateTime.now())
            .logActionId((short)2)
            .accountNumber("1234ACC")
            .businessUnitId((short)3)
            .jsonRequest("{\"request\":\"processAudit\"}")
            .build();
    }
}
