package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B_ENABLED_PROPERTY;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.GetDefendantAccountImpositionsResponse;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountHeaderSummary200Response;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionsResponseCommon;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchReferenceNumberDefendantAccount;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.generated.model.GetEnforcementStatusResponse;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchResponseDefendantAccount;
import uk.gov.hmcts.opal.mapper.history.DefendantAccountHistoryResponseMapper;
import uk.gov.hmcts.opal.service.DefendantAccountService;
import uk.gov.hmcts.opal.service.ImpositionService;

@ExtendWith(MockitoExtension.class)
class DefendantAccountApiControllerTest {

    @Mock
    private DefendantAccountService defendantAccountService;

    @Mock
    private ImpositionService impositionService;

    @Mock
    private DefendantAccountHistoryResponseMapper defendantAccountHistoryResponseMapper;

    @InjectMocks
    private DefendantAccountApiController defendantAccountApiController;

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
    void given_validRequest_when_getDefendantAccountHistory_then_returnsOkResponse() {
        Long defendantId = 1L;
        DefendantAccountHistoryResponse historyResponse = DefendantAccountHistoryResponse.builder()
            .version(BigInteger.ONE)
            .build();
        GetDefendantAccountHistoryResponse generatedResponse = new GetDefendantAccountHistoryResponse();

        when(defendantAccountService.getHistory(eq(defendantId), eq(null), eq(null), eq(List.of())))
            .thenReturn(historyResponse);
        when(defendantAccountHistoryResponseMapper.toGeneratedResponse(historyResponse))
            .thenReturn(generatedResponse);

        ResponseEntity<GetDefendantAccountHistoryResponse> response =
            defendantAccountApiController.getDefendantAccountHistory(defendantId, null, null, List.of());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("\"1\"", response.getHeaders().getETag());
        assertSame(generatedResponse, response.getBody());
        verify(defendantAccountService).getHistory(eq(defendantId), eq(null), eq(null), eq(List.of()));
        verify(defendantAccountHistoryResponseMapper).toGeneratedResponse(historyResponse);
    }

    @Test
    void given_queryValues_when_getDefendantAccountHistory_then_delegatesRawValuesToService() {
        Long defendantId = 1L;
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 1, 31);
        List<String> itemTypes = List.of("note,paymentTerms", "enforcement");
        DefendantAccountHistoryResponse historyResponse = DefendantAccountHistoryResponse.builder().build();
        when(defendantAccountService.getHistory(eq(defendantId), eq(dateFrom), eq(dateTo), eq(itemTypes)))
            .thenReturn(historyResponse);
        when(defendantAccountHistoryResponseMapper.toGeneratedResponse(historyResponse))
            .thenReturn(new GetDefendantAccountHistoryResponse());

        defendantAccountApiController.getDefendantAccountHistory(defendantId, dateFrom, dateTo, itemTypes);

        verify(defendantAccountService).getHistory(eq(defendantId), eq(dateFrom), eq(dateTo), eq(itemTypes));
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

}
