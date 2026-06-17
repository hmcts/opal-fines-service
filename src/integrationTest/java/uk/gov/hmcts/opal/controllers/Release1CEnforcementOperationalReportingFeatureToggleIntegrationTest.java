package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
 * Verifies that all Release 1C Enforcement Operational Reporting endpoints guarded by @FeatureToggle return 405 when
 * the release-1c-enforcement-operational-reporting flag is disabled.
 */
@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.Release1CEnforcementOperationalReportingFeatureToggleIntegrationTest")
@DisplayName("Release 1C Enforcement Operational Reporting - returns 405 when flag is disabled")
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1c-enforcement-operational-reporting=false"
})
class Release1CEnforcementOperationalReportingFeatureToggleIntegrationTest
    extends AbstractFeatureToggleIntegrationTest {

    static Stream<Arguments> release1cEnforcementOperationalReportingEndpoints() {
        return Stream.of(
            // ReportsApiController
            args("GET /reports/{id}", withAuth(get("/reports/1"))),
            args("POST /report-instances", withAuthAndJson(post("/report-instances")
                .content("{\"business_unit_ids\":[1],\"report_id\":\"report-id\",\"report_parameters\":{}}")))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("release1cEnforcementOperationalReportingEndpoints")
    @DisplayName("should return 404 Not Found")
    @JiraStory("PO-2250")
    @JiraStory("PO-2252")
    @JiraEpic("PO-2248")
    void shouldReturn404When1cEnforcementOperationalReportingIsDisabled(String description,
        MockHttpServletRequestBuilder request)
        throws Exception {
        log.debug("Testing feature-disabled 404 for: {}", description);
        mockMvc.perform(request)
            .andExpect(status().isNotFound());
    }
}
