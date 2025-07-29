package uk.gov.hmcts.opal.launchdarkly;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.config.properties.LaunchDarklyProperties;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureToggleApiTest {

    private static final String FAKE_FEATURE = "fake-feature";
    private static final String FAKE_ENVIRONMENT = "fake-env";
    private static final String FAKE_KEY = "fake-key";

    @Mock
    private LDClientInterface ldClient;

    @Captor
    private ArgumentCaptor<LDContext> ldContextArgumentCaptor;

    private FeatureToggleApi featureToggleApi;

    @BeforeEach
    void setUp() {
        LaunchDarklyProperties launchDarklyProperties = new LaunchDarklyProperties();
        launchDarklyProperties.setEnv(FAKE_ENVIRONMENT);
        launchDarklyProperties.setSdkKey(FAKE_KEY);
        launchDarklyProperties.setEnabled(true);
        featureToggleApi = new FeatureToggleApi(ldClient, launchDarklyProperties);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectState_whenUserIsProvided(Boolean toggleState) {
        LDContext ldContext = LDContext.builder(FAKE_KEY)
            .set("timestamp", String.valueOf(System.currentTimeMillis()))
            .set("environment", FAKE_ENVIRONMENT).build();
        givenToggle(FAKE_FEATURE, toggleState);

        assertThat(featureToggleApi.isFeatureEnabled(FAKE_FEATURE, ldContext)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(
            FAKE_FEATURE,
            ldContext,
            false
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectState_whenDefaultServiceUser(Boolean toggleState) {
        givenToggle(FAKE_FEATURE, toggleState);

        assertThat(featureToggleApi.isFeatureEnabled(FAKE_FEATURE)).isEqualTo(toggleState);
        verifyBoolVariationCalled(FAKE_FEATURE, List.of("timestamp", "environment"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"opal", "legacy"})
    void shouldReturnCorrectStringValue_whenDefaultServiceUser(String toggleState) {
        when(ldClient.stringVariation(eq(FAKE_FEATURE), any(LDContext.class), any()))
            .thenReturn(toggleState);

        assertThat(featureToggleApi.getFeatureValue(FAKE_FEATURE, null)).isEqualTo(toggleState);
    }

    private void givenToggle(String feature, boolean state) {
        when(ldClient.boolVariation(eq(feature), any(LDContext.class), anyBoolean()))
            .thenReturn(state);
    }

    private void verifyBoolVariationCalled(String feature, List<String> customAttributesKeys) {
        verify(ldClient).boolVariation(
            eq(feature),
            ldContextArgumentCaptor.capture(),
            eq(false)
        );

        var capturedLdContext = ldContextArgumentCaptor.getValue();
        assertThat(capturedLdContext.getKey()).isEqualTo(FAKE_KEY);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectState_whenUserWithLocationIsProvided(Boolean toggleState) {
        LDContext ldContext = LDContext.builder(FAKE_KEY)
            .set("timestamp", String.valueOf(System.currentTimeMillis()))
            .set("environment", FAKE_ENVIRONMENT)
            .set("location", "000000")
            .build();

        givenToggle(FAKE_FEATURE, toggleState);

        assertThat(featureToggleApi.isFeatureEnabled(FAKE_FEATURE, ldContext)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(
            FAKE_FEATURE,
            ldContext,
            false
        );
    }
}
