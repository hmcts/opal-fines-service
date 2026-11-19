package uk.gov.hmcts.opal.testsupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

class IntegrationContextFamiliesTest {

    @ParameterizedTest
    @CsvSource({
        "integration, integrationBase",
        "opal, integrationOpal",
        "legacy, integrationLegacy",
        "integration-with-spring-security, integrationSecurity"
    })
    void selectsEffectiveProfile(String profile, String expected) {
        assertThat(IntegrationContextFamilies.family(List.of(profile), Map.of())).isEqualTo(expected);
    }

    @Test
    void inlineModeOverridesProfile() {
        assertThat(IntegrationContextFamilies.family(List.of("legacy"), mode("false")))
            .isEqualTo("integrationOpal");
        assertThat(IntegrationContextFamilies.family(List.of("integration"), mode("true")))
            .isEqualTo("integrationLegacy");
    }

    @Test
    void securityTakesPrecedenceOverModes() {
        assertThat(IntegrationContextFamilies.family(List.of("integration-with-spring-security", "opal"), mode("true")))
            .isEqualTo("integrationSecurity");
    }

    @Test
    void rejectsAmbiguousAndUnresolvedModes() {
        assertThatIllegalArgumentException().isThrownBy(() ->
            IntegrationContextFamilies.family(List.of("opal", "legacy"), Map.of()));
        assertThatIllegalArgumentException().isThrownBy(() ->
            IntegrationContextFamilies.family(List.of("integration"), mode("${test.mode}")));
    }

    @Test
    void resolvesInheritedContextMetadataWithoutLoadingContext() {
        var result = IntegrationContextFamilies.classify(InheritedLegacy.class, List.of(InheritedLegacy.class));
        assertThat(result.task()).isEqualTo("integrationLegacy");
        assertThat(result.className()).isEqualTo(InheritedLegacy.class.getName());
        assertThat(result.profiles()).contains("integration");
    }

    @Test
    void keepsMixedNestedModesTogetherInBase() {
        var result = IntegrationContextFamilies.classify(OpalContext.class,
            List.of(OpalContext.class, LegacyContext.class));
        assertThat(result.task()).isEqualTo("integrationBase");
        assertThat(result.reason()).contains("keep nested tests together");
    }

    @Test
    void nestedSecurityContextSelectsSecurityOnce() {
        var result = IntegrationContextFamilies.classify(OpalContext.class,
            List.of(OpalContext.class, SecurityContext.class));
        assertThat(result.task()).isEqualTo("integrationSecurity");
    }

    @Test
    void nonSpringIntegrationTestBelongsToBase() {
        assertThat(IntegrationContextFamilies.classify(PlainTest.class, List.of(PlainTest.class)).task())
            .isEqualTo("integrationBase");
    }

    @Test
    void resolvesEnclosingSpringConfigurationForNestedTests() {
        var result = IntegrationContextFamilies.classify(OpalParent.class,
            List.of(OpalParent.class, OpalParent.Child.class));
        assertThat(result.task()).isEqualTo("integrationOpal");
    }

    private static Map<String, String> mode(String value) {
        return Map.of("launchdarkly.default-flag-values.is-legacy-mode", value);
    }

    @ContextConfiguration(classes = UnloadedConfiguration.class)
    @ActiveProfiles("integration")
    @TestPropertySource(properties = "launchdarkly.default-flag-values.is-legacy-mode=true")
    private static class LegacyContext {
    }

    private static class InheritedLegacy extends LegacyContext {
    }

    @ContextConfiguration(classes = UnloadedConfiguration.class)
    @ActiveProfiles("opal")
    private static class OpalContext {
    }

    @ContextConfiguration(classes = UnloadedConfiguration.class)
    @ActiveProfiles("integration-with-spring-security")
    private static class SecurityContext {
    }

    private static class PlainTest {
    }

    @ContextConfiguration(classes = UnloadedConfiguration.class)
    @ActiveProfiles("opal")
    private static class OpalParent {
        @Nested
        class Child {
        }
    }

    private static class UnloadedConfiguration {
        static {
            if (true) {
                throw new IllegalStateException("Discovery must not initialise Spring configuration classes");
            }
        }
    }
}
