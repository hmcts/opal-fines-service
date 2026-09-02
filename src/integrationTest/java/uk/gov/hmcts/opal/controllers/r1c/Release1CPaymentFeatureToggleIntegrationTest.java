package uk.gov.hmcts.opal.controllers.r1c;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import uk.gov.hmcts.opal.controllers.shared.AbstractFeatureToggleIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

/**
 * Verifies that all Release 1C Payment endpoints guarded by @FeatureToggle return 404 when the release-1c-payment
 * flag is disabled.
 */
@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.Release1CPaymentFeatureToggleIntegrationTest")
@DisplayName("Release 1C Payment - returns 404 when flag is disabled")
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1c-payment=false"
})
class Release1CPaymentFeatureToggleIntegrationTest extends AbstractFeatureToggleIntegrationTest {

    private static final String OUTSTANDING_AUTO_PAYMENT_PATH = "/business-units/outstanding-auto-payment-count";
    private static final String INTERFACE_JOBS_PATH = "/testing-support/interface-jobs";

    static Stream<Arguments> release1cPaymentEndpoints() {
        return Stream.of(
            endpoint("GET " + OUTSTANDING_AUTO_PAYMENT_PATH, get(OUTSTANDING_AUTO_PAYMENT_PATH)),
            endpoint("DELETE " + INTERFACE_JOBS_PATH, delete(INTERFACE_JOBS_PATH).queryParam("ids", "1")));
    }

    private static Arguments endpoint(String description, MockHttpServletRequestBuilder request) {
        return args(description, withAuth(request));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("release1cPaymentEndpoints")
    @DisplayName("should return 404 Not Found")
    @JiraStory("PO-2470")
    @JiraStory("PO-2578")
    @JiraEpic("PO-2468")
    void shouldReturn404When1cPaymentIsDisabled(String description, MockHttpServletRequestBuilder request)
        throws Exception {
        log.debug("Testing feature-disabled 404 for: {}", description);
        mockMvc.perform(request).andExpect(status().isNotFound());
    }
}
