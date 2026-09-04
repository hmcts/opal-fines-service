package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B_ENABLED_PROPERTY;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.GetDefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountConsolidatedAccountsResult;
import uk.gov.hmcts.opal.dto.GetDefendantAccountImpositionsResponse;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.generated.model.AddEnforcementRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.AddEnforcementResponseDefendantAccount;
import uk.gov.hmcts.opal.generated.model.AtAGlanceResponseDefendantAccount;
import uk.gov.hmcts.opal.generated.model.ConsolidatedAccountDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionsResponseCommon;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchReferenceNumberDefendantAccount;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountHeaderSummary200Response;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.generated.model.FixedPenaltyTicketDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.VehicleFixedPenaltyDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.GetEnforcementStatusResponse;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchResponseDefendantAccount;
import uk.gov.hmcts.opal.generated.model.RemoveDefendantAccountPartyDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.RemoveDefendantAccountPartyRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.RemoveDefendantAccountPartyResponseDefendantAccount;
import uk.gov.hmcts.opal.generated.model.RemoveEnforcementHoldRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.RemoveEnforcementHoldResponseDefendantAccount;
import uk.gov.hmcts.opal.mapper.history.DefendantAccountHistoryResponseMapper;
import uk.gov.hmcts.opal.service.DefendantAccountEnforcementService;
import uk.gov.hmcts.opal.service.DefendantAccountPartyService;
import uk.gov.hmcts.opal.service.DefendantAccountPaymentTermsService;
import uk.gov.hmcts.opal.service.DefendantAccountService;
import uk.gov.hmcts.opal.service.DefendantAccountFixedPenaltyService;
import uk.gov.hmcts.opal.service.ImpositionService;

@ExtendWith(MockitoExtension.class)
class DefendantAccountApiControllerTest {

    @Mock
    private DefendantAccountService defendantAccountService;

    @Mock
    private ImpositionService impositionService;

    @Mock
    private DefendantAccountEnforcementService defendantAccountEnforcementService;

    @Mock
    private DefendantAccountHistoryResponseMapper defendantAccountHistoryResponseMapper;

    @Mock
    private DefendantAccountPartyService defendantAccountPartyService;

    @Mock
    private DefendantAccountPaymentTermsService defendantAccountPaymentTermsService;

    @Mock
    private DefendantAccountFixedPenaltyService defendantAccountFixedPenaltyService;

    @InjectMocks
    private DefendantAccountApiController defendantAccountApiController;

    @Test
    void whenAddEnforcement_thenReturnsMappedResponse() {
        AddEnforcementRequestDefendantAccount request = new AddEnforcementRequestDefendantAccount();
        AddEnforcementResponseDefendantAccount mappedResponse = AddEnforcementResponseDefendantAccount.builder()
            .defendantAccountId("1")
            .enforcementId("2")
            .version(BigInteger.ONE)
            .build();

        when(defendantAccountEnforcementService.addEnforcement(1L, (short) 77, "1", request))
            .thenReturn(mappedResponse);

        ResponseEntity<AddEnforcementResponseDefendantAccount> response =
            defendantAccountApiController.addEnforcement(1L, (short) 77, request, "1");

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals(BigInteger.ONE, response.getBody().getVersion()),
            () -> assertSame(mappedResponse, response.getBody()),
            () -> verify(defendantAccountEnforcementService).addEnforcement(1L, (short) 77, "1", request)
        );
    }

    @Test
    void given_validRequest_when_getDefendantAccountFixedPenalty_then_returnsOkResponseWithEtag() {
        Long defendantAccountId = 77L;
        GetDefendantAccountFixedPenaltyResponse serviceResponse = GetDefendantAccountFixedPenaltyResponse.builder()
            .vehicleFixedPenaltyFlag(true)
            .fixedPenaltyTicketDetails(FixedPenaltyTicketDetailsCommonStrict.builder()
                .issuingAuthority("Kingston-upon-Thames Mags Court")
                .ticketNumber("888")
                .timeOfOffence("12:34")
                .placeOfOffence("London")
                .build())
            .vehicleFixedPenaltyDetails(VehicleFixedPenaltyDetailsCommonStrict.builder()
                .vehicleRegistrationNumber("AB12CDE")
                .vehicleDriversLicense("DOE1234567")
                .noticeNumber("PN98765")
                .dateNoticeIssued(LocalDate.of(2024, 1, 1))
                .build())
            .version(BigInteger.valueOf(12))
            .build();
        when(defendantAccountFixedPenaltyService.getDefendantAccountFixedPenalty(defendantAccountId))
            .thenReturn(serviceResponse);

        ResponseEntity<GetDefendantAccountFixedPenaltyResponse> response =
            defendantAccountApiController.getDefendantAccountFixedPenalty(defendantAccountId);

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals("\"12\"", response.getHeaders().getETag()),
            () -> assertSame(serviceResponse, response.getBody()),
            () -> verify(defendantAccountFixedPenaltyService)
                .getDefendantAccountFixedPenalty(defendantAccountId)
        );
    }

    @Test
    void given_validRequest_when_getImpositions_then_returnsOkResponseWithEtag() {
        Long defendantId = 1L;
        DefendantAccountImpositionsResponseCommon payload = new DefendantAccountImpositionsResponseCommon();
        GetDefendantAccountImpositionsResponse serviceResponse = GetDefendantAccountImpositionsResponse.builder()
            .payload(payload)
            .version(BigInteger.valueOf(12))
            .build();
        when(impositionService.getImpositions(defendantId))
            .thenReturn(serviceResponse);

        ResponseEntity<DefendantAccountImpositionsResponseCommon> response =
            defendantAccountApiController.getImpositions(defendantId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("\"12\"", response.getHeaders().getETag());
        assertSame(payload, response.getBody());
        verify(impositionService).getImpositions(defendantId);
    }

    @Test
    void getImpositions_isProtectedByRelease1BFeatureToggle() throws NoSuchMethodException {
        Method method = DefendantAccountApiController.class.getMethod(
            "getImpositions", Long.class);

        FeatureToggle featureToggle = method.getAnnotation(FeatureToggle.class);

        assertNotNull(featureToggle);
        assertEquals(RELEASE_1B, featureToggle.feature());
        assertEquals(RELEASE_1B_ENABLED_PROPERTY, featureToggle.defaultValueProperty());
    }

    @Test
    void given_validRequest_when_getConsolidatedAccounts_then_returnsOkResponseWithEtag() {
        Long defendantId = 1L;
        List<ConsolidatedAccountDefendantAccount> payload = List.of();
        GetDefendantAccountConsolidatedAccountsResult serviceResponse =
            GetDefendantAccountConsolidatedAccountsResult.builder()
                .payload(payload)
                .version(BigInteger.valueOf(7))
                .build();
        when(defendantAccountService.getConsolidatedAccounts(defendantId))
            .thenReturn(serviceResponse);

        ResponseEntity<List<ConsolidatedAccountDefendantAccount>> response =
            defendantAccountApiController.getConsolidatedAccounts(defendantId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("\"7\"", response.getHeaders().getETag());
        assertSame(payload, response.getBody());
        verify(defendantAccountService).getConsolidatedAccounts(defendantId);
    }

    @Test
    void given_validRequest_when_getDefendantAccountAtAGlance_then_returnsOkResponseWithEtag() {
        Long defendantId = 77L;
        AtAGlanceResponseDefendantAccount payload =
            AtAGlanceResponseDefendantAccount.builder()
                .defendantAccountId("77")
                .accountNumber("177A")
                .debtorType(AtAGlanceResponseDefendantAccount.DebtorTypeEnum.DEFENDANT)
                .isYouth(false)
                .build();
        GetDefendantAccountAtAGlanceResponse serviceResponse = GetDefendantAccountAtAGlanceResponse.builder()
            .payload(payload)
            .version(BigInteger.valueOf(5))
            .build();

        when(defendantAccountService.getAtAGlance(defendantId)).thenReturn(serviceResponse);

        ResponseEntity<AtAGlanceResponseDefendantAccount> response =
            defendantAccountApiController.getDefendantAccountAtAGlance(defendantId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("\"5\"", response.getHeaders().getETag());
        assertSame(payload, response.getBody());
        verify(defendantAccountService).getAtAGlance(defendantId);
    }

    @Test
    void getConsolidatedAccounts_isProtectedByRelease1BFeatureToggle() throws NoSuchMethodException {
        Method method = DefendantAccountApiController.class.getMethod(
            "getConsolidatedAccounts", Long.class);

        FeatureToggle featureToggle = method.getAnnotation(FeatureToggle.class);

        assertNotNull(featureToggle);
        assertEquals(RELEASE_1B, featureToggle.feature());
        assertEquals(RELEASE_1B_ENABLED_PROPERTY, featureToggle.defaultValueProperty());
    }

    @Test
    void given_validRequest_when_getEnforcementStatus_then_returnsOkResponse() {
        Long defendantId = 1L;
        EnforcementStatus status = EnforcementStatus.builder()
            .build();
        when(defendantAccountService.getEnforcementStatus(defendantId))
            .thenReturn(status);

        ResponseEntity<GetEnforcementStatusResponse> response =
            defendantAccountApiController.getEnforcementStatus(defendantId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(status, response.getBody());
        verify(defendantAccountService).getEnforcementStatus(defendantId);
    }

    @Test
    void given_validRequest_when_postDefendantAccountSearch_then_returnsOkResponse() {
        PostDefendantAccountSearchRequestDefendantAccount request =
            PostDefendantAccountSearchRequestDefendantAccount.builder()
                .activeAccountsOnly(true)
                .businessUnitIds(List.of((short) 101))
                .referenceNumber(new DefendantAccountSearchReferenceNumberDefendantAccount()
                    .organisation(false)
                    .accountNumber("AC123"))
                .build();
        PostDefendantAccountSearchResponseDefendantAccount serviceResponse =
            PostDefendantAccountSearchResponseDefendantAccount.builder()
                .count(1)
                .defendantAccounts(List.of())
                .build();

        when(defendantAccountService.searchDefendantAccounts(request)).thenReturn(serviceResponse);

        ResponseEntity<PostDefendantAccountSearchResponseDefendantAccount> response =
            defendantAccountApiController.postDefendantAccountSearch(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(serviceResponse, response.getBody());
        verify(defendantAccountService).searchDefendantAccounts(request);
    }

    @Test
    void given_validRequest_when_removeDefendantAccountParty_then_returnsOkResponseWithEtag() {
        Long defendantAccountId = 1L;
        Long defendantAccountPartyId = 10L;
        Short businessUnitId = 10;
        String ifMatch = "\"1\"";
        RemoveDefendantAccountPartyRequestDefendantAccount request =
            RemoveDefendantAccountPartyRequestDefendantAccount.builder()
                .defendantAccountPartyId("10")
                .partyDetails(RemoveDefendantAccountPartyDetailsCommonStrict.builder()
                    .partyId("10")
                    .build())
                .build();
        RemoveDefendantAccountPartyResponseDefendantAccount serviceResponse =
            RemoveDefendantAccountPartyResponseDefendantAccount.builder()
                .defendantAccountPartyId("10")
                .version(BigInteger.valueOf(2))
                .build();
        DefendantAccountApiController controller = new DefendantAccountApiController(
            defendantAccountService,
            defendantAccountEnforcementService,
            defendantAccountHistoryResponseMapper,
            impositionService,
            defendantAccountPartyService,
            defendantAccountFixedPenaltyService,
            defendantAccountPaymentTermsService
        );

        when(defendantAccountPartyService.removeDefendantAccountParty(
            eq(defendantAccountId),
            eq(defendantAccountPartyId),
            eq(businessUnitId),
            eq(ifMatch),
            any(RemoveDefendantAccountPartyRequestDefendantAccount.class)))
            .thenReturn(serviceResponse);

        ResponseEntity<RemoveDefendantAccountPartyResponseDefendantAccount> response =
            controller.removeDefendantAccountParty(
                defendantAccountId, defendantAccountPartyId, businessUnitId, request, ifMatch);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("\"2\"", response.getHeaders().getETag());
        assertNotNull(response.getBody());
        assertEquals("10", response.getBody().getDefendantAccountPartyId());

        ArgumentCaptor<RemoveDefendantAccountPartyRequestDefendantAccount> requestCaptor =
            ArgumentCaptor.forClass(RemoveDefendantAccountPartyRequestDefendantAccount.class);
        verify(defendantAccountPartyService).removeDefendantAccountParty(
            eq(defendantAccountId),
            eq(defendantAccountPartyId),
            eq(businessUnitId),
            eq(ifMatch),
            requestCaptor.capture());
        assertEquals("10", requestCaptor.getValue().getDefendantAccountPartyId());
    }

    @Test
    void given_validRequest_when_getDefendantAccountHistory_then_returnsOkResponse() {
        Long defendantId = 1L;
        DefendantAccountHistoryResponse historyResponse = DefendantAccountHistoryResponse.builder()
            .version(BigInteger.ONE)
            .build();
        GetDefendantAccountHistoryResponse generatedResponse = new GetDefendantAccountHistoryResponse();

        when(defendantAccountService.getHistory(defendantId, null, null, List.of()))
            .thenReturn(historyResponse);
        when(defendantAccountHistoryResponseMapper.toGeneratedResponse(historyResponse))
            .thenReturn(generatedResponse);

        ResponseEntity<GetDefendantAccountHistoryResponse> response =
            defendantAccountApiController.getDefendantAccountHistory(defendantId, null, null, List.of());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("\"1\"", response.getHeaders().getETag());
        assertSame(generatedResponse, response.getBody());
        verify(defendantAccountService).getHistory(defendantId, null, null, List.of());
        verify(defendantAccountHistoryResponseMapper).toGeneratedResponse(historyResponse);
    }

    @Test
    void given_queryValues_when_getDefendantAccountHistory_then_delegatesRawValuesToService() {
        Long defendantId = 1L;
        LocalDate dateFrom = LocalDate.of(2026, Month.JANUARY, 1);
        LocalDate dateTo = LocalDate.of(2026, Month.JANUARY, 31);
        List<String> itemTypes = List.of("note,paymentTerms", "enforcement");
        DefendantAccountHistoryResponse historyResponse = DefendantAccountHistoryResponse.builder().build();
        when(defendantAccountService.getHistory(defendantId, dateFrom, dateTo, itemTypes))
            .thenReturn(historyResponse);
        when(defendantAccountHistoryResponseMapper.toGeneratedResponse(historyResponse))
            .thenReturn(new GetDefendantAccountHistoryResponse());

        defendantAccountApiController.getDefendantAccountHistory(defendantId, dateFrom, dateTo, itemTypes);

        verify(defendantAccountService).getHistory(defendantId, dateFrom, dateTo, itemTypes);
        verify(defendantAccountHistoryResponseMapper).toGeneratedResponse(historyResponse);
    }

    @Test
    void given_validRequest_when_getDefendantAccountHeaderSummary_then_returnsOkResponse() {
        Long defendantId = 1L;
        GetDefendantAccountHeaderSummary200Response summaryResponse =
            GetDefendantAccountHeaderSummary200Response.builder().build();
        DefendantAccountHeaderSummary summary =
            DefendantAccountHeaderSummary.builder().version(BigInteger.ONE).response(summaryResponse).build();
        when(defendantAccountService.getHeaderSummary(defendantId)).thenReturn(summary);

        ResponseEntity<GetDefendantAccountHeaderSummary200Response> response =
            defendantAccountApiController.getDefendantAccountHeaderSummary(defendantId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("\"1\"", response.getHeaders().getETag());
        assertSame(summaryResponse, response.getBody());
        verify(defendantAccountService).getHeaderSummary(defendantId);
    }

    @Test
    void whenRemovingEnforcementHold_thenReturnsGeneratedResponseWithEtag() {
        Long defendantAccountId = 1L;
        Short businessUnitId = 10;
        String ifMatch = "\"7\"";
        RemoveEnforcementHoldRequestDefendantAccount request =
            RemoveEnforcementHoldRequestDefendantAccount.builder().reason("remove hold reason").build();
        RemoveEnforcementHoldResponseDefendantAccount serviceResponse =
            RemoveEnforcementHoldResponseDefendantAccount.builder()
                .defendantAccountId("1")
                .version(BigInteger.valueOf(8))
                .build();
        when(defendantAccountEnforcementService.removeEnforcementHold(
            defendantAccountId, businessUnitId, ifMatch, request)).thenReturn(serviceResponse);

        ResponseEntity<RemoveEnforcementHoldResponseDefendantAccount> response =
            defendantAccountApiController.removeEnforcementHold(
                defendantAccountId, businessUnitId, request, ifMatch);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("\"8\"", response.getHeaders().getETag());
        assertSame(serviceResponse, response.getBody());
        verify(defendantAccountEnforcementService).removeEnforcementHold(
            defendantAccountId, businessUnitId, ifMatch, request);
    }

}
