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
import uk.gov.hmcts.opal.entity.LogActionEntity;
import uk.gov.hmcts.opal.entity.LogAuditDetailEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnit;
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

    private static final String URL_BASE = "/dev/log-audit-details/";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    @Qualifier("logAuditDetailServiceProxy")
    LogAuditDetailService logAuditDetailService;

    @Test
    void testGetLogAuditDetailById() throws Exception {
        LogAuditDetailEntity logAuditDetailEntity = createLogAuditDetailEntity();

        when(logAuditDetailService.getLogAuditDetail(1L)).thenReturn(logAuditDetailEntity);

        mockMvc.perform(get(URL_BASE + "1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.logAuditDetailId").value(1))
            .andExpect(jsonPath("$.userId").value(4))
            .andExpect(jsonPath("$.logAction.logActionId").value(2))
            .andExpect(jsonPath("$.accountNumber").value("1234ACC"))
            .andExpect(jsonPath("$.businessUnit.businessUnitId").value(3))
            .andExpect(jsonPath("$.jsonRequest").value("{\"request\":\"processAudit\"}"));
    }


    @Test
    void testGetLogAuditDetailById_WhenLogAuditDetailDoesNotExist() throws Exception {
        when(logAuditDetailService.getLogAuditDetail(2L)).thenReturn(null);

        mockMvc.perform(get(URL_BASE + "2"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testPostLogAuditDetailsSearch() throws Exception {
        LogAuditDetailEntity logAuditDetailEntity = createLogAuditDetailEntity();

        when(logAuditDetailService.searchLogAuditDetails(any(LogAuditDetailSearchDto.class)))
            .thenReturn(singletonList(logAuditDetailEntity));

        mockMvc.perform(post(URL_BASE + "search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"value\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].logAuditDetailId").value(1))
            .andExpect(jsonPath("$[0].userId").value(4))
            .andExpect(jsonPath("$[0].logAction.logActionId").value(2))
            .andExpect(jsonPath("$[0].accountNumber").value("1234ACC"))
            .andExpect(jsonPath("$[0].businessUnit.businessUnitId").value(3))
            .andExpect(jsonPath("$[0].jsonRequest").value("{\"request\":\"processAudit\"}"));
    }

    @Test
    void testPostLogAuditDetailsSearch_WhenLogAuditDetailDoesNotExist() throws Exception {
        mockMvc.perform(post(URL_BASE + "search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isOk());
    }

    private LogAuditDetailEntity createLogAuditDetailEntity() {
        return LogAuditDetailEntity.builder()
            .logAuditDetailId(1L)
            .userId(4L)
            .logTimestamp(LocalDateTime.now())
            .logAction(LogActionEntity.builder().logActionId((short)2).logActionName("ActionX").build())
            .accountNumber("1234ACC")
            .businessUnit(BusinessUnit.Lite.builder().businessUnitId((short)3).businessUnitName("BU3").build())
            .jsonRequest("{\"request\":\"processAudit\"}")
            .build();
    }
}
