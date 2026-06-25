package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import uk.gov.hmcts.opal.controllers.util.DraftAccountTestData;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;


/**
 * Verifies that all Release 1A endpoints guarded by @FeatureToggle return 404 when the release-1a flag is disabled.
 */
@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.Release1AFeatureToggleIntegrationTest")
@DisplayName("Release 1A - returns 404 when release-1a flag is disabled")
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1a=false"
})
class Release1AFeatureToggleIntegrationTest extends AbstractFeatureToggleIntegrationTest {

    private static final String DRAFT_ACCOUNT_ID = "/draft-accounts/100000";

    private static List<Arguments> release1aEndpointArguments() {
        return List.of(
            // BusinessUnitController
            args("GET /business-units/{id}", withAuth(get("/business-units/1"))),
            args("GET /business-units", withAuth(get("/business-units").param("q", ""))),

            // CourtController
            args("GET /courts/{id}", withAuth(get("/courts/1"))),
            args("POST /courts/search", withAuthAndJson(post("/courts/search").content("{}"))),
            args("GET /courts", withAuth(get("/courts").param("q", ""))),

            // DraftAccountController
            args("GET /draft-accounts/{id}", withAuth(get(DRAFT_ACCOUNT_ID))),
            args("GET /draft-accounts", withAuth(get("/draft-accounts"))),
            args("POST /draft-accounts", withAuthAndJson(post("/draft-accounts")
                .content(DraftAccountTestData.Post.defaultData().toJson()))),
            args("PUT /draft-accounts/{id}", withAuthJsonAndIfMatch(put(DRAFT_ACCOUNT_ID)
                .content(DraftAccountTestData.Put.defaultData().toJson()))),
            args("PATCH /draft-accounts/{id}", withAuthJsonAndIfMatch(patch(DRAFT_ACCOUNT_ID)
                .content(DraftAccountTestData.Patch.defaultData().toJson()))),

            // LocalJusticeAreaController
            args("GET /local-justice-areas/{id}", withAuth(get("/local-justice-areas/1"))),
            args("GET /local-justice-areas", withAuth(get("/local-justice-areas")
                .param("q", "1").param("lja_type", "LJA,SJCRT"))),

            // MajorCreditorController
            args("GET /major-creditors/{id}", withAuth(get("/major-creditors/1"))),
            args("GET /major-creditors", withAuth(get("/major-creditors")
                .param("q", ""))),

            // OffenceController
            args("GET /offences/{id}", withAuth(get("/offences/290434"))),
            args("POST /offences/search", withAuthAndJson(post("/offences/search").content("{}"))),
            args("GET /offences", withAuth(get("/offences").param("q", ""))),

            // ProsecutorController
            args("GET /prosecutors/{id}", withAuth(get("/prosecutors/1"))),
            args("GET /prosecutors", withAuth(get("/prosecutors").param("q", ""))),

            // ResultController
            args("GET /results", withAuth(get("/results")))
        );
    }

    static Stream<Arguments> remainingRelease1aEndpoints() {
        return release1aEndpointArguments().stream().skip(1);
    }

    @Test
    @DisplayName("should return 404 Not Found")
    @JiraStory("PO-2833")
    @JiraEpic("PO-2352")
    @JiraTestKey("PO-6232")
    void shouldReturn404WhenRelease1aIsDisabled_firstEndpoint() throws Exception {
        Object[] values = release1aEndpointArguments().getFirst().get();
        assertRelease1aDisabled((String) values[0], (MockHttpServletRequestBuilder) values[1]);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("remainingRelease1aEndpoints")
    @JiraStory("PO-2833")
    @JiraEpic("PO-2352")
    @JiraTestKey("PO-8120")
    void shouldReturn404WhenRelease1aIsDisabled(String description, MockHttpServletRequestBuilder request)
        throws Exception {
        assertRelease1aDisabled(description, request);
    }

    private void assertRelease1aDisabled(String description, MockHttpServletRequestBuilder request) throws Exception {
        log.debug("Testing feature-disabled 404 for: {}", description);
        mockMvc.perform(request)
            .andExpect(status().isNotFound());
    }
}
