package uk.gov.hmcts.opal.controllers.r1b;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.exception.PdplLoggingException;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

class DefendantAccountViewPdplLoggingIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    @MockitoBean
    private LoggingService loggingService;

    @BeforeEach
    void setup() {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(false);
    }

    @Test
    @JiraStory("PO-10756")
    @JiraEpic("PO-10729")
    void testDefendantAccountViewHeaderSummary_fails() throws Exception {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(false);

        ResultActions resultActions = mockMvc.perform(
            get("/defendant-accounts/77/header-summary")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
        );

        resultActions.andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/pdpl-logging-unavailable"))
            .andExpect(jsonPath("$.detail").value("The personal data logging queue is currently unavailable"))
            .andExpect(jsonPath("$.retriable").value(true))
            .andExpect(result -> assertInstanceOf(PdplLoggingException.class, result.getResolvedException()));
    }

    @Test
    @JiraStory("PO-10756")
    @JiraEpic("PO-10729")
    void testDefendantAccountViewAtAGlance_fails() throws Exception {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(false);

        ResultActions resultActions = mockMvc.perform(
            get("/defendant-accounts/77/at-a-glance")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
        );

        resultActions.andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/pdpl-logging-unavailable"))
            .andExpect(jsonPath("$.detail").value("The personal data logging queue is currently unavailable"))
            .andExpect(jsonPath("$.retriable").value(true))
            .andExpect(result -> assertInstanceOf(PdplLoggingException.class, result.getResolvedException()));
    }

    @Test
    @JiraStory("PO-10756")
    @JiraEpic("PO-10729")
    void testDefendantAccountViewHeaderSummary_company_noLogging() throws Exception {
        ResultActions resultActions = mockMvc.perform(
            get("/defendant-accounts/10001/header-summary")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
        );

        resultActions.andExpect(status().isOk());
        verifyNoInteractions(loggingService);
    }

    @Test
    @JiraStory("PO-10756")
    @JiraEpic("PO-10729")
    void testDefendantAccountViewAtAGlance_company_noLogging() throws Exception {
        ResultActions resultActions = mockMvc.perform(
            get("/defendant-accounts/10001/at-a-glance")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
        );

        resultActions.andExpect(status().isOk());
        verifyNoInteractions(loggingService);
    }
}
