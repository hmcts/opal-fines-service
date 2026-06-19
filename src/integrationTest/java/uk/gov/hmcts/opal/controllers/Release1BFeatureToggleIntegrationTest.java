package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;


/**
 * Verifies that all Release 1B endpoints guarded by @FeatureToggle return 404 when the release-1b flag is disabled.
 */
@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.Release1BFeatureToggleIntegrationTest")
@DisplayName("Release 1B - returns 404 when release-1b flag is disabled")
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=false"
})
class Release1BFeatureToggleIntegrationTest extends AbstractFeatureToggleIntegrationTest {

    static Stream<Arguments> release1bEndpoints() {
        return Stream.of(
            // BusinessUnitController
            args("GET /defendant-accounts/{id}/impositions", withAuth(get("/defendant-accounts/1/impositions")))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("release1bEndpoints")
    @DisplayName("should return 404 Not Found")
    @JiraStory("PO-2077")
    @JiraEpic("PO-979")
    void shouldReturn404WhenRelease1bIsDisabled(String description, MockHttpServletRequestBuilder request)
        throws Exception {
        log.debug("Testing feature-disabled 404 for: {}", description);
        mockMvc.perform(request)
            .andExpect(status().isNotFound());
    }
}
