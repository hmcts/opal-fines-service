package uk.gov.hmcts.opal.controllers.r1b;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.SchemaPaths.GET_DEFENDANT_ACCOUNT_IMPOSITIONS_RESPONSE;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.controllers.shared.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.Creditor;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.CreditorAccountType;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.Imposition;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.Offence;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.PostedDetails;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountImpositionsLegacyResponse.Result;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetImpositionsRequest;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.legacy.LegacyImpositionService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "legacy"})
@DisplayName("Legacy Defendant Account Impositions Integration Tests")
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
class LegacyDefAccImpositionsTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/defendant-accounts";
    private static final String AUTH_HEADER = "Bearer test-token";

    @MockitoBean
    private UserStateService userStateService;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @MockitoBean
    private UserStateClientService userStateClientService;

    @MockitoBean
    private GatewayService gatewayService;

    @MockitoSpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    @BeforeEach
    void setupUserState() {
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(UserStateUtil.allPermissionsUser());
    }

    @Test
    @DisplayName("LEGACY: Get Defendant Account Impositions returns schema-valid imposition response")
    @JiraStory("PO-2078")
    @JiraEpic("PO-979")
    @JiraTestKey("PO-8267")
    void getImpositions_returnsLegacyImpositionResponse() throws Exception {
        ArgumentCaptor<LegacyGetImpositionsRequest> requestCaptor =
            ArgumentCaptor.forClass(LegacyGetImpositionsRequest.class);

        when(gatewayService.postToGateway(
            eq(LegacyImpositionService.GET_IMPOSITIONS),
            eq(GetDefendantAccountImpositionsLegacyResponse.class),
            requestCaptor.capture(),
            isNull()
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse(), null, null));

        MvcResult result = performGetImpositions(12345L)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"18338687664539878704807506660830801130000030354\""))
            .andExpect(jsonPath("$.impositions", hasSize(1)))
            .andExpect(jsonPath("$.impositions[0].date_added").value("2026-08-19"))
            .andExpect(jsonPath("$.impositions[0].date_imposed").value("2025-05-15"))
            .andExpect(jsonPath("$.impositions[0].imposition.result_id").value("FO"))
            .andExpect(jsonPath("$.impositions[0].imposition.result_title")
                .value("FINE"))
            .andExpect(jsonPath("$.impositions[0].creditor.creditor_account_id").value(77L))
            .andExpect(jsonPath("$.impositions[0].creditor.account_type").value("CF"))
            .andExpect(jsonPath("$.impositions[0].creditor.display_name").value("Central Fund"))
            .andExpect(jsonPath("$.impositions[0].creditor.name").value("HM Courts & Tribunals Service"))
            .andExpect(jsonPath("$.impositions[0].imposed_amount").value(-250.00))
            .andExpect(jsonPath("$.impositions[0].paid_amount").value(300.00))
            .andExpect(jsonPath("$.impositions[0].balance").value(50.00))
            .andExpect(jsonPath("$.impositions[0].offence.id").value(33369L))
            .andExpect(jsonPath("$.impositions[0].offence.code").value("HY35014"))
            .andExpect(jsonPath("$.impositions[0].offence.title").value("Riding a bicycle on a footpath"))
            .andExpect(jsonPath("$.impositions[0].imposition_id").value(770000027211L))
            .andReturn();

        jsonSchemaValidationService.validateOrError(
            result.getResponse().getContentAsString(),
            GET_DEFENDANT_ACCOUNT_IMPOSITIONS_RESPONSE
        );

        verify(gatewayService).postToGateway(
            eq(LegacyImpositionService.GET_IMPOSITIONS),
            eq(GetDefendantAccountImpositionsLegacyResponse.class),
            eq(requestCaptor.getValue()),
            isNull()
        );
        assertEquals("12345", requestCaptor.getValue().getDefendantAccountId());
    }

    @Test
    @DisplayName("LEGACY: Get Defendant Account Impositions returns 403 when user lacks permission")
    @JiraStory("PO-2078")
    @JiraEpic("PO-979")
    @JiraTestKey("PO-8266")
    void getImpositions_whenUserLacksPermission_returnsForbidden() throws Exception {
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(UserStateUtil.noPermissionsUser());

        performGetImpositions(12345L)
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"))
            .andExpect(jsonPath("$.status").value(403));

        verifyNoInteractions(gatewayService);
    }

    @Test
    @DisplayName("LEGACY: Get Defendant Account Impositions returns 404 when legacy gateway returns not found")
    @JiraStory("PO-2078")
    @JiraEpic("PO-979")
    @JiraTestKey("PO-8268")
    void getImpositions_whenGatewayReturnsNotFound_returnsNotFound() throws Exception {
        when(gatewayService.postToGateway(
            eq(LegacyImpositionService.GET_IMPOSITIONS),
            eq(GetDefendantAccountImpositionsLegacyResponse.class),
            any(),
            isNull()
        )).thenThrow(HttpClientErrorException.create(
            HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, null, null));

        performGetImpositions(99999L)
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(404));
    }

    private ResultActions performGetImpositions(Long defendantAccountId) throws Exception {
        return mockMvc.perform(get(URL_BASE + "/" + defendantAccountId + "/impositions")
                                   .header("Authorization", AUTH_HEADER)
                                   .accept(MediaType.APPLICATION_JSON));
    }

    private GetDefendantAccountImpositionsLegacyResponse legacyResponse() {
        return GetDefendantAccountImpositionsLegacyResponse.builder()
            .version(new BigInteger("18338687664539878704807506660830801130000030354"))
            .impositions(List.of(Imposition.builder()
                .postedDetails(PostedDetails.builder()
                    .postedDate(LocalDateTime.parse("2026-08-19T00:00:00.00001"))
                    .postedBy("L077AO")
                    .postedByName("L077AO")
                    .build())
                .dateImposed(LocalDate.parse("2025-05-15"))
                .result(Result.builder()
                    .resultId("FO")
                    .resultTitle("FINE")
                    .build())
                .creditor(Creditor.builder()
                    .creditorAccountType(CreditorAccountType.builder().creditorAccountType("CF").build())
                    .creditorAccountId(77L)
                    .majorCreditorName("HM Courts & Tribunals Service")
                    .build())
                .imposedAmount(new BigDecimal("-250.00"))
                .paidAmount(new BigDecimal("300.00"))
                .balance(new BigDecimal("50.00"))
                .offence(Offence.builder()
                    .offenceId(33369L)
                    .cjsCode("HY35014")
                    .offenceTitle("Riding a bicycle on a footpath")
                    .build())
                .impositionId(770000027211L)
                .build()))
            .build();
    }
}
