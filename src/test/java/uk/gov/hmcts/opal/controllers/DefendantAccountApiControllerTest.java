package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.math.BigInteger;
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
import uk.gov.hmcts.opal.generated.model.GetEnforcementStatusResponse;
import uk.gov.hmcts.opal.service.DefendantAccountService;


@ExtendWith(MockitoExtension.class)
class DefendantAccountApiControllerTest {

    private static final String BEARER_TOKEN = "Bearer a_token_goes_here";

    @Mock
    private DefendantAccountService defendantAccountService;

    @InjectMocks
    private DefendantAccountApiController defendantAccountApiController;

    @Test
    void given_validRequest_when_getDefendantAccountImpositions_then_returnsOkResponseWithEtag() {
        Long defendantId = 1L;
        DefendantAccountImpositionsResponseCommon payload = new DefendantAccountImpositionsResponseCommon();
        GetDefendantAccountImpositionsResponse serviceResponse = GetDefendantAccountImpositionsResponse.builder()
            .payload(payload)
            .version(BigInteger.valueOf(12))
            .build();
        when(defendantAccountService.getDefendantAccountImpositions(defendantId, BEARER_TOKEN))
            .thenReturn(serviceResponse);

        ResponseEntity<DefendantAccountImpositionsResponseCommon> response =
            defendantAccountApiController.getDefendantAccountImpositions(defendantId, BEARER_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("\"12\"", response.getHeaders().getETag());
        assertSame(payload, response.getBody());
        verify(defendantAccountService).getDefendantAccountImpositions(defendantId, BEARER_TOKEN);
    }

    @Test
    void getDefendantAccountImpositions_isProtectedByRelease1bFeatureToggle() throws NoSuchMethodException {
        Method method = DefendantAccountApiController.class.getMethod(
            "getDefendantAccountImpositions", Long.class, String.class);

        FeatureToggle featureToggle = method.getAnnotation(FeatureToggle.class);

        assertNotNull(featureToggle);
        assertEquals("release-1b", featureToggle.feature());
        assertEquals("launchdarkly.default-flag-values.release-1b", featureToggle.defaultValueProperty());
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

}
