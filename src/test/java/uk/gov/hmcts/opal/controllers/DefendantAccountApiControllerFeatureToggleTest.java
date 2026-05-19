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

class DefendantAccountApiControllerFeatureToggleTest {

    private static final Long DEFENDANT_ACCOUNT_ID = 77L;
    private static final String AUTH_HEADER = "Bearer token";
    private static final String RELEASE_1B_DEFAULT_PROPERTY = "launchdarkly.default-flag-values.release-1b";

    @Test
    void getDefendantAccountImpositions_whenRelease1bEnabled_proceedsToService() {
        DefendantAccountService service = mock(DefendantAccountService.class);
        DefendantAccountImpositionsResponseCommon payload = new DefendantAccountImpositionsResponseCommon();
        when(service.getDefendantAccountImpositions(DEFENDANT_ACCOUNT_ID, AUTH_HEADER))
            .thenReturn(GetDefendantAccountImpositionsResponse.builder()
                            .payload(payload)
                            .version(BigInteger.valueOf(4))
                            .build());

        DefendantAccountApiController controller = proxiedController(service, true);

        ResponseEntity<DefendantAccountImpositionsResponseCommon> response =
            controller.getDefendantAccountImpositions(DEFENDANT_ACCOUNT_ID, AUTH_HEADER);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("\"4\"", response.getHeaders().getETag());
        assertSame(payload, response.getBody());
        verify(service).getDefendantAccountImpositions(DEFENDANT_ACCOUNT_ID, AUTH_HEADER);
    }

    @Test
    void getDefendantAccountImpositions_whenRelease1bDisabled_throwsFeatureDisabledWithoutCallingService() {
        DefendantAccountService service = mock(DefendantAccountService.class);
        DefendantAccountApiController controller = proxiedController(service, false);

        FeatureDisabledException exception = assertThrows(
            FeatureDisabledException.class,
            () -> controller.getDefendantAccountImpositions(DEFENDANT_ACCOUNT_ID, AUTH_HEADER)
        );

        assertEquals(
            "Feature release-1b is not enabled for method getDefendantAccountImpositions",
            exception.getMessage()
        );
        verifyNoInteractions(service);
    }

    private DefendantAccountApiController proxiedController(
        DefendantAccountService service,
        boolean release1bDefaultValue) {

        LaunchDarklyProperties launchDarklyProperties = mock(LaunchDarklyProperties.class);
        when(launchDarklyProperties.isEnabled()).thenReturn(false);

        MockEnvironment environment = new MockEnvironment()
            .withProperty(RELEASE_1B_DEFAULT_PROPERTY, Boolean.toString(release1bDefaultValue));

        FeatureToggleAspect featureToggleAspect = new FeatureToggleAspect(
            mock(FeatureToggleApi.class),
            launchDarklyProperties,
            environment
        );

        AspectJProxyFactory proxyFactory = new AspectJProxyFactory(new DefendantAccountApiController(service));
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAspect(featureToggleAspect);
        return proxyFactory.getProxy();
    }
}
