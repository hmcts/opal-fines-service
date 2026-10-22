package uk.gov.hmcts.opal.controllers.r1b;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.SEARCH_AND_VIEW_ACCOUNTS;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.VIEW_CREDITOR_BACS;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.controllers.shared.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "legacy"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
@DisplayName("Minor Creditor Account At A Glance Legacy Error Integration Tests")
class LegacyMinorCreditorAtAGlanceErrorIntegrationTest extends AbstractIntegrationTest {

    private static final String AUTH_HEADER = "Bearer test-token";
    private static final String URL = "/minor-creditor-accounts/{id}/at-a-glance";
    private static final String LEGACY_OPERATION = "LIBRA.get_minor_creditors_account_at_a_glance";
    private static final long MINOR_CREDITOR_ACCOUNT_ID = 99_000_000_000_802L;

    @MockitoBean
    private UserStateService userStateService;

    @MockitoBean
    private GatewayService gatewayService;

    @BeforeEach
    void setUp() {
        when(userStateService.getUserStateV1FromSecurityContext())
            .thenReturn(UserStateUtil.permissionUser((short) 77,
                SEARCH_AND_VIEW_ACCOUNTS, VIEW_CREDITOR_BACS));
    }

    @Test
    @DisplayName("PO-10678 returns 408 when the legacy request times out")
    @JiraStory("PO-10678")
    @JiraEpic("PO-2982")
    void getAtAGlance_whenLegacyRequestTimesOutReturns408() throws Exception {
        stubGatewayException(HttpClientErrorException.create(
            HttpStatusCode.valueOf(408), "Request Timeout", HttpHeaders.EMPTY, null, null));

        getAtAGlance()
            .andExpect(status().isRequestTimeout())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(header().exists("operation_id"))
            .andExpect(jsonPath("$.status").value(408))
            .andExpect(jsonPath("$.title").value("Request Timeout"))
            .andExpect(jsonPath("$.retriable").value(false))
            .andExpect(jsonPath("$.party").doesNotExist())
            .andExpect(jsonPath("$.defendant").doesNotExist());
    }

    @Test
    @DisplayName("PO-10678 returns 503 when a downstream resource is unavailable")
    @JiraStory("PO-10678")
    @JiraEpic("PO-2982")
    void getAtAGlance_whenDownstreamResourceUnavailableReturns503() throws Exception {
        stubGatewayException(new DataAccessResourceFailureException("Legacy gateway unavailable"));

        getAtAGlance()
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(header().exists("operation_id"))
            .andExpect(jsonPath("$.status").value(503))
            .andExpect(jsonPath("$.title").value("Service Unavailable"))
            .andExpect(jsonPath("$.retriable").value(true))
            .andExpect(jsonPath("$.party").doesNotExist())
            .andExpect(jsonPath("$.defendant").doesNotExist());
    }

    @Test
    @DisplayName("PO-10678 returns 500 for an unexpected legacy error")
    @JiraStory("PO-10678")
    @JiraEpic("PO-2982")
    void getAtAGlance_whenUnexpectedLegacyErrorReturns500() throws Exception {
        stubGatewayException(HttpServerErrorException.create(
            HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpHeaders.EMPTY, null, null));

        getAtAGlance()
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(header().exists("operation_id"))
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.title").value("Downstream Server Error"))
            .andExpect(jsonPath("$.retriable").value(false))
            .andExpect(jsonPath("$.party").doesNotExist())
            .andExpect(jsonPath("$.defendant").doesNotExist());
    }

    private void stubGatewayException(RuntimeException exception) {
        when(gatewayService.postToGateway(
            eq(LEGACY_OPERATION),
            eq(LegacyGetMinorCreditorAccountAtAGlanceResponse.class),
            any(),
            isNull()
        )).thenThrow(exception);
    }

    private ResultActions getAtAGlance() throws Exception {
        return mockMvc.perform(get(URL, MINOR_CREDITOR_ACCOUNT_ID)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER));
    }
}
