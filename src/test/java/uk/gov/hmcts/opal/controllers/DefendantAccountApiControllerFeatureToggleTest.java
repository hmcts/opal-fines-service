package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.env.MockEnvironment;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureDisabledException;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggleAspect;
import uk.gov.hmcts.opal.common.launchdarkly.config.LaunchDarklyProperties;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.dto.GetDefendantAccountImpositionsResponse;
import uk.gov.hmcts.opal.generated.model.DefendantAccountImpositionsResponseCommon;
import uk.gov.hmcts.opal.service.DefendantAccountService;
import uk.gov.hmcts.opal.service.ImpositionService;
import uk.gov.hmcts.opal.util.FeatureFlags;

class DefendantAccountApiControllerFeatureToggleTest {

    private static final Long DEFENDANT_ACCOUNT_ID = 77L;
    private static final String AUTH_HEADER = "Bearer token";

    @Test
    void getImpositions_whenRelease1bEnabled_proceedsToService() {
        DefendantAccountService defendantAccountService = mock(DefendantAccountService.class);
        ImpositionService impositionService = mock(ImpositionService.class);
        DefendantAccountImpositionsResponseCommon payload = new DefendantAccountImpositionsResponseCommon();
        when(impositionService.getImpositions(DEFENDANT_ACCOUNT_ID, AUTH_HEADER))
            .thenReturn(GetDefendantAccountImpositionsResponse.builder()
                            .payload(payload)
                            .version(BigInteger.valueOf(4))
                            .build());

        DefendantAccountApiController controller = proxiedController(defendantAccountService, impositionService, true);

        ResponseEntity<DefendantAccountImpositionsResponseCommon> response =
            controller.getImpositions(DEFENDANT_ACCOUNT_ID, AUTH_HEADER);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("\"4\"", response.getHeaders().getETag());
        assertSame(payload, response.getBody());
        verify(impositionService).getImpositions(DEFENDANT_ACCOUNT_ID, AUTH_HEADER);
    }

    @Test
    void getImpositions_whenRelease1bDisabled_throwsFeatureDisabledWithoutCallingService() {
        DefendantAccountService defendantAccountService = mock(DefendantAccountService.class);
        ImpositionService impositionService = mock(ImpositionService.class);
        DefendantAccountApiController controller = proxiedController(defendantAccountService, impositionService, false);

        FeatureDisabledException exception = assertThrows(
            FeatureDisabledException.class,
            () -> controller.getImpositions(DEFENDANT_ACCOUNT_ID, AUTH_HEADER)
        );

        assertEquals(
            "Feature release-1b is not enabled for method getImpositions",
            exception.getMessage()
        );
        verifyNoInteractions(defendantAccountService, impositionService);
    }

    private DefendantAccountApiController proxiedController(
        DefendantAccountService defendantAccountService,
        ImpositionService impositionService,
        boolean release1bDefaultValue) {

        LaunchDarklyProperties launchDarklyProperties = mock(LaunchDarklyProperties.class);
        when(launchDarklyProperties.isEnabled()).thenReturn(false);

        MockEnvironment environment = new MockEnvironment()
            .withProperty(FeatureFlags.RELEASE_1B_DEFAULT_VALUE_PROPERTY, Boolean.toString(release1bDefaultValue));

        FeatureToggleAspect featureToggleAspect = new FeatureToggleAspect(
            mock(FeatureToggleApi.class),
            launchDarklyProperties,
            environment
        );

        AspectJProxyFactory proxyFactory =
            new AspectJProxyFactory(new DefendantAccountApiController(defendantAccountService, impositionService));
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAspect(featureToggleAspect);
        return proxyFactory.getProxy();
    }
}
