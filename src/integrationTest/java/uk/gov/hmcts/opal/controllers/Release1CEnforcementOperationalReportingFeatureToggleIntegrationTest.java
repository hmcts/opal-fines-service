package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

/**
 * Verifies that all Release 1C Enforcement Operational Reporting endpoints guarded by @FeatureToggle return 404 when
 * the release-1c-enforcement-operational-reporting flag is disabled.
 */
@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.Release1CEnforcementOperationalReportingFeatureToggleIntegrationTest")
@DisplayName("Release 1C Enforcement Operational Reporting - returns 404 when flag is disabled")
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1c-enforcement-operational-reporting=false"
})
class Release1CEnforcementOperationalReportingFeatureToggleIntegrationTest
    extends AbstractFeatureToggleIntegrationTest {

    private static List<Arguments> release1cEndpointArguments() {
        return List.of(
            // ReportsApiController
            args("GET /reports/{id}", withAuth(get("/reports/1"))),
            // ReportInstancesApiController
            args("GET /report-instances", withAuth(get("/report-instances"))),
            args("POST /report-instances", withAuthAndJson(post("/report-instances")
                .content("{\"business_unit_ids\":[1],\"report_id\":\"report-id\",\"report_parameters\":{}}"))),
            args("GET /report-instances/{id}", withAuth(get("/report-instances/1")))
        );
    }

    static Stream<Arguments> remainingRelease1cEnforcementOperationalReportingEndpoints() {
        return release1cEndpointArguments().stream().skip(1);
    }

    @Test
    @DisplayName("should return 404 Not Found")
    @JiraStory("PO-2250")
    @JiraStory("PO-2252")
    @JiraStory("PO-2254")
    @JiraEpic("PO-2248")
    @JiraTestKey("PO-7661")
    void shouldReturn404When1cEnforcementOperationalReportingIsDisabled_firstEndpoint() throws Exception {
        Object[] values = release1cEndpointArguments().getFirst().get();
        assertFeatureDisabled((String) values[0], (MockHttpServletRequestBuilder) values[1]);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("remainingRelease1cEnforcementOperationalReportingEndpoints")
    @JiraTestKey("PO-8139")
    void shouldReturn404When1cEnforcementOperationalReportingIsDisabled(String description,
        MockHttpServletRequestBuilder request)
        throws Exception {
        assertFeatureDisabled(description, request);
    }

    private void assertFeatureDisabled(String description, MockHttpServletRequestBuilder request) throws Exception {
        log.debug("Testing feature-disabled 404 for: {}", description);
        mockMvc.perform(request)
            .andExpect(status().isNotFound());
    }
}
