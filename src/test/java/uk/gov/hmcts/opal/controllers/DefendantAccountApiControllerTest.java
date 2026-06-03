package uk.gov.hmcts.opal.controllers;

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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.GetDefendantAccountImpositionsResponse;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionsResponseCommon;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountHistory200Response;
import uk.gov.hmcts.opal.generated.model.GetEnforcementStatusResponse;
import uk.gov.hmcts.opal.mapper.history.DefendantAccountHistoryResponseMapper;
import uk.gov.hmcts.opal.service.DefendantAccountService;
import uk.gov.hmcts.opal.service.ImpositionService;

@ExtendWith(MockitoExtension.class)
class DefendantAccountApiControllerTest {

    private static final String BEARER_TOKEN = "Bearer a_token_goes_here";

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
        when(impositionService.getImpositions(defendantId, BEARER_TOKEN))
            .thenReturn(serviceResponse);

        ResponseEntity<DefendantAccountImpositionsResponseCommon> response =
            defendantAccountApiController.getImpositions(defendantId, BEARER_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("\"12\"", response.getHeaders().getETag());
        assertSame(payload, response.getBody());
        verify(impositionService).getImpositions(defendantId, BEARER_TOKEN);
    }

    @Test
    void getImpositions_isProtectedByRelease1BFeatureToggle() throws NoSuchMethodException {
        Method method = DefendantAccountApiController.class.getMethod(
            "getImpositions", Long.class, String.class);

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
        when(defendantAccountService.getEnforcementStatus(defendantId, BEARER_TOKEN))
            .thenReturn(status);

        ResponseEntity<GetEnforcementStatusResponse> response =
            defendantAccountApiController.getEnforcementStatus(defendantId, BEARER_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(status, response.getBody());
        verify(defendantAccountService).getEnforcementStatus(defendantId, BEARER_TOKEN);
    }

    @Test
    void given_validRequest_when_getDefendantAccountHistory_then_returnsOkResponse() {
        Long defendantId = 1L;
        DefendantAccountHistoryResponse historyResponse = DefendantAccountHistoryResponse.builder().build();
        GetDefendantAccountHistory200Response generatedResponse = new GetDefendantAccountHistory200Response();

        when(defendantAccountService.getHistory(eq(defendantId), any(DefendantAccountHistoryFilter.class),
            eq(BEARER_TOKEN)))
            .thenReturn(historyResponse);
        when(defendantAccountHistoryResponseMapper.toGeneratedResponse(historyResponse))
            .thenReturn(generatedResponse);

        ResponseEntity<GetDefendantAccountHistory200Response> response =
            defendantAccountApiController.getDefendantAccountHistory(defendantId, null, null, List.of(), BEARER_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(generatedResponse, response.getBody());
        verify(defendantAccountService).getHistory(eq(defendantId), any(DefendantAccountHistoryFilter.class),
            eq(BEARER_TOKEN));
        verify(defendantAccountHistoryResponseMapper).toGeneratedResponse(historyResponse);
    }

}
