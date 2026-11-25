package uk.gov.hmcts.opal.controllers.r1c;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.opal.controllers.shared.AbstractFeatureToggleIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles("integration")
@Slf4j(topic = "opal.PrisonsRelease1CFeatureToggleIntegrationTest")
@DisplayName("Prisons Release 1C - returns 404 when release-1c is disabled")
@Tag("ExtendedTest")
@JiraStory("PO-11025")
@JiraEpic("PO-10769")
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1c=false"
})
class PrisonsRelease1CFeatureToggleIntegrationTest extends AbstractFeatureToggleIntegrationTest {

    static Stream<Arguments> prisonEndpoints() {
        return Stream.of(
            args("GET /prisons", withAuth(get("/prisons"))),
            args("GET /prisons/{id}", withAuth(get("/prisons/1")))
        );
    }

    @ParameterizedTest(name = "{0}")
    @JiraStory("PO-11025")
    @JiraEpic("PO-10769")
    @MethodSource("prisonEndpoints")
    @JiraTestKey("PO-11025")
    void shouldReturn404WhenRelease1cIsDisabled(
        String description, MockHttpServletRequestBuilder request) throws Exception {
        log.debug("Testing feature-disabled 404 for: {}", description);
        mockMvc.perform(request).andExpect(status().isNotFound());
    }
}
