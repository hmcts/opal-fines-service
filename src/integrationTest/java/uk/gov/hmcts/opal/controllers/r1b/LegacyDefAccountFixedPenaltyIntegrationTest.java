package uk.gov.hmcts.opal.controllers.r1b;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountGetFixedPenaltyRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountGetFixedPenaltyResponse;
import uk.gov.hmcts.opal.dto.legacy.VehicleFixedPenaltyDetails;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles(profiles = {"integration", "legacy"})
@DisplayName("Legacy Defendant Account Fixed Penalty Integration Tests")
@TestPropertySource(properties = {"launchdarkly.enabled=false", "launchdarkly.default-flag-values.release-1b=true"})
public class LegacyDefAccountFixedPenaltyIntegrationTest extends AbstractIntegrationTest {

    private static final String BASE_URL = "/defendant-accounts/";

    @MockitoBean
    private GatewayService gatewayService;

    @Test
    @JiraEpic("PO-1676")
    @JiraStory("PO-10338")
    @DisplayName("LEGACY: Get Defendant Account Fixed Penalty Returns Valid Legacy Fixed Penalty Response")
    void getDefendantAccountFixedPenalty_returnsLegacyFixedPenaltyResponse() throws Exception {
        ArgumentCaptor<LegacyDefendantAccountGetFixedPenaltyRequest> requestCaptor =
            ArgumentCaptor.forClass(LegacyDefendantAccountGetFixedPenaltyRequest.class);

        when(gatewayService.postToGateway(eq("getDefendantAccountFixedPenalty"),
            eq(LegacyDefendantAccountGetFixedPenaltyResponse.class), requestCaptor.capture(), isNull()))
            .thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse(), null, null));

        ResultActions resultActions = mockMvc.perform(get(BASE_URL + 12345L + "/fixed-penalty")
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header("authorization", userStateStub.getBearerToken())
            .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"1054510671152783513749174862079571170070000027455\""))
            .andExpect(jsonPath("$.vehicle_fixed_penalty_flag").value(true))
            .andExpect(jsonPath("$.vehicle_fixed_penalty_details.date_notice_issued").value("2025-08-01"))
            .andExpect(jsonPath("$.vehicle_fixed_penalty_details.notice_number").value("NTO"))
            .andExpect(jsonPath("$.vehicle_fixed_penalty_details.vehicle_drivers_license").value("SMITH001010JO9MS"))
            .andExpect(jsonPath("$.vehicle_fixed_penalty_details.vehicle_registration_number").value("REG1"));

        verify(gatewayService).postToGateway(eq("getDefendantAccountFixedPenalty"),
            eq(LegacyDefendantAccountGetFixedPenaltyResponse.class), eq(requestCaptor.getValue()), isNull());
        assertEquals(12345L, requestCaptor.getValue().getDefendantAccountId());
    }

    @Test
    @JiraEpic("PO-1676")
    @JiraStory("PO-10338")
    @DisplayName("LEGACY: Get Defendant Account Fixed Penalty Returns 403 Response")
    void getDefendantAccountFixedPenalty_returnsForbidden() throws Exception {
        ResultActions resultActions = mockMvc.perform(get(BASE_URL + 999999L + "/fixed-penalty")
            .with(userStateStub.getInvalidAuthenticaitonRequestPostProcessor())
            .header("authorization", userStateStub.getBearerToken())
            .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail").value("You do not have permission to access this resource"))
            .andExpect(jsonPath("$.status").value(403)).andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"));
    }

    @Test
    @JiraEpic("PO-1676")
    @JiraStory("PO-10338")
    @DisplayName("LEGACY: Get Defendant Account Fixed Penalty Returns 404 Response")
    void getDefendantAccountFixedPenalty_returnsNotFound() throws Exception {
        when(gatewayService.postToGateway(eq("getDefendantAccountFixedPenalty"),
            eq(LegacyDefendantAccountGetFixedPenaltyResponse.class), any(), isNull())).thenThrow(
            HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Request was not matched", HttpHeaders.EMPTY, null,
                null));

        ResultActions resultActions = mockMvc.perform(get(BASE_URL + 999999L + "/fixed-penalty")
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header("authorization", userStateStub.getBearerToken())
            .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail", containsString("Request was not matched")))
            .andExpect(jsonPath("$.status").value(404)).andExpect(jsonPath("$.title").value("Not Found"))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/http-client-error"));
    }

    private LegacyDefendantAccountGetFixedPenaltyResponse legacyResponse() {
        return LegacyDefendantAccountGetFixedPenaltyResponse.builder()
            .version(new BigInteger("1054510671152783513749174862079571170070000027455"))
            .vehicleFixedPenaltyFlag(true)
            .vehicleFixedPenaltyDetails(VehicleFixedPenaltyDetails.builder()
                .fpRegistrationNumber("REG1")
                .fpDrivingLicense("SMITH001010JO9MS")
                .noticeToOwnerOrHirerNumber("NTO")
                .dateNoticeIssued("2025-08-01")
                .build())
            .build();
    }
}
