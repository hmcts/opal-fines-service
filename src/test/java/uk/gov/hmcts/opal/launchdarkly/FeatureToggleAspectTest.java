package uk.gov.hmcts.opal.launchdarkly;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    FeatureToggleAspect.class,
    FeatureToggleApi.class
})
class FeatureToggleAspectTest {

    private static final String NEW_FEATURE = "NEW_FEATURE";
    @Autowired
    FeatureToggleAspect featureToggleAspect;

    @MockBean
    LDClient ldClient;
    @MockBean
    ProceedingJoinPoint proceedingJoinPoint;
    @MockBean
    FeatureToggle featureToggle;
    @MockBean
    MethodSignature methodSignature;

    @BeforeEach
    void setUp() {
        when(featureToggle.feature()).thenReturn(NEW_FEATURE);
        when(proceedingJoinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getName()).thenReturn("myFeatureToggledMethod");
    }

    @SneakyThrows
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldProceedToMethodInvocation_whenFeatureToggleIsEnabled(Boolean state) {
        when(featureToggle.value()).thenReturn(state);
        givenToggle(NEW_FEATURE, state);

        featureToggleAspect.checkFeatureEnabled(proceedingJoinPoint, featureToggle);

        verify(proceedingJoinPoint).proceed();
    }

    @SneakyThrows
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldNotProceedToMethodInvocation_whenFeatureToggleIsDisabled(Boolean state) {
        when(featureToggle.value()).thenReturn(state);
        givenToggle(NEW_FEATURE, !state);

        featureToggleAspect.checkFeatureEnabled(proceedingJoinPoint, featureToggle);

        verify(proceedingJoinPoint, never()).proceed();
    }

    private void givenToggle(String feature, boolean state) {
        when(ldClient.boolVariation(eq(feature), any(LDUser.class), anyBoolean()))
            .thenReturn(state);
    }
}
