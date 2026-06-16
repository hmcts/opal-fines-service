package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
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
import java.time.LocalDate;
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
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.legacy.LegacyCourtReferenceCommon;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountImpositionCommon;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountImpositionsResponseCommon;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetImpositionsRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyImpositionCreditorReferenceCommon;
import uk.gov.hmcts.opal.dto.legacy.LegacyOffenceReferenceCommon;
import uk.gov.hmcts.opal.dto.legacy.LegacyResultReferenceCommon;
import uk.gov.hmcts.opal.generated.model.ImpositionCreditorReferenceCommon.AccountTypeEnum;
import uk.gov.hmcts.opal.generated.model.ImpositionCreditorReferenceCommon.DisplayNameEnum;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.legacy.LegacyImpositionService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "legacy"})
@DisplayName("Legacy Defendant Account Impositions Integration Tests")
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
class LegacyDefendantAccountImpositionsIntegrationTest extends AbstractIntegrationTest {

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
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allPermissionsUser());
    }

    @Test
    @DisplayName("LEGACY: Get Defendant Account Impositions returns schema-valid imposition response")
    @JiraStory("PO-2078")
    @JiraEpic("PO-979")
    void getImpositions_returnsLegacyImpositionResponse() throws Exception {
        ArgumentCaptor<LegacyGetImpositionsRequest> requestCaptor =
            ArgumentCaptor.forClass(LegacyGetImpositionsRequest.class);

        when(gatewayService.postToGateway(
            eq(LegacyImpositionService.GET_IMPOSITIONS),
            eq(LegacyDefendantAccountImpositionsResponseCommon.class),
            requestCaptor.capture(),
            isNull()
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse(), null, null));

        MvcResult result = performGetImpositions(12345L)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"4\""))
            .andExpect(jsonPath("$.impositions", hasSize(1)))
            .andExpect(jsonPath("$.impositions[0].date_added").value("2026-05-06"))
            .andExpect(jsonPath("$.impositions[0].imposition.result_id").value("ABDC"))
            .andExpect(jsonPath("$.impositions[0].imposition.result_title")
                .value("Application made for Benefit Deductions"))
            .andExpect(jsonPath("$.impositions[0].creditor.creditor_account_id").value(99000000000806L))
            .andExpect(jsonPath("$.impositions[0].creditor.account_type").value("MN"))
            .andExpect(jsonPath("$.impositions[0].creditor.display_name").value("Minor Creditor"))
            .andExpect(jsonPath("$.impositions[0].creditor.major_creditor_id").value(is(nullValue())))
            .andExpect(jsonPath("$.impositions[0].creditor.minor_creditor_party_id").value(99000000000906L))
            .andExpect(jsonPath("$.impositions[0].creditor.name").value("Metropolitan Traffic Unit"))
            .andExpect(jsonPath("$.impositions[0].imposed_amount").value(600.00))
            .andExpect(jsonPath("$.impositions[0].paid_amount").value(60.00))
            .andExpect(jsonPath("$.impositions[0].balance").value(540.00))
            .andExpect(jsonPath("$.impositions[0].date_imposed").value("2026-05-05"))
            .andExpect(jsonPath("$.impositions[0].offence.id").value(5510))
            .andExpect(jsonPath("$.impositions[0].offence.code").value("OFF0006"))
            .andExpect(jsonPath("$.impositions[0].offence.title").value("Test Offence 6"))
            .andExpect(jsonPath("$.impositions[0].imposed_by.court_id").value(101))
            .andExpect(jsonPath("$.impositions[0].imposed_by.court_code").value(102))
            .andExpect(jsonPath("$.impositions[0].imposed_by.court_name").value("Legacy Court"))
            .andExpect(jsonPath("$.impositions[0].imposition_id").value(99000000003006L))
            .andReturn();

        jsonSchemaValidationService.validateOrError(
            result.getResponse().getContentAsString(),
            GET_DEFENDANT_ACCOUNT_IMPOSITIONS_RESPONSE
        );

        verify(gatewayService).postToGateway(
            eq(LegacyImpositionService.GET_IMPOSITIONS),
            eq(LegacyDefendantAccountImpositionsResponseCommon.class),
            eq(requestCaptor.getValue()),
            isNull()
        );
        assertEquals("12345", requestCaptor.getValue().getDefendantAccountId());
    }

    @Test
    @DisplayName("LEGACY: Get Defendant Account Impositions returns 403 when user lacks permission")
    @JiraStory("PO-2078")
    @JiraEpic("PO-979")
    void getImpositions_whenUserLacksPermission_returnsForbidden() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.noPermissionsUser());

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
    void getImpositions_whenGatewayReturnsNotFound_returnsNotFound() throws Exception {
        when(gatewayService.postToGateway(
            eq(LegacyImpositionService.GET_IMPOSITIONS),
            eq(LegacyDefendantAccountImpositionsResponseCommon.class),
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

    private LegacyDefendantAccountImpositionsResponseCommon legacyResponse() {
        return LegacyDefendantAccountImpositionsResponseCommon.builder()
            .version(4L)
            .impositions(List.of(LegacyDefendantAccountImpositionCommon.builder()
                .dateAdded(LocalDate.parse("2026-05-06"))
                .dateImposed(LocalDate.parse("2026-05-05"))
                .imposition(LegacyResultReferenceCommon.builder()
                    .resultId("ABDC")
                    .resultTitle("Application made for Benefit Deductions")
                    .build())
                .creditor(LegacyImpositionCreditorReferenceCommon.builder()
                    .creditorAccountId(99000000000806L)
                    .accountType(AccountTypeEnum.MN)
                    .displayName(DisplayNameEnum.MINOR_CREDITOR)
                    .minorCreditorPartyId(99000000000906L)
                    .name("Metropolitan Traffic Unit")
                    .build())
                .imposedAmount(new BigDecimal("600.00"))
                .paidAmount(new BigDecimal("60.00"))
                .balance(new BigDecimal("540.00"))
                .offence(LegacyOffenceReferenceCommon.builder()
                    .id(5510L)
                    .code("OFF0006")
                    .title("Test Offence 6")
                    .build())
                .imposedBy(LegacyCourtReferenceCommon.builder()
                    .courtId(101L)
                    .courtCode(102)
                    .courtName("Legacy Court")
                    .build())
                .impositionId(99000000003006L)
                .build()))
            .build();
    }
}
