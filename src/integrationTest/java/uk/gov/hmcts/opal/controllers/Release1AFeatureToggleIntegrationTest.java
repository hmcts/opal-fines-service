package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import uk.gov.hmcts.opal.controllers.util.DraftAccountTestData;


/**
 * Verifies that all Release 1A endpoints guarded by @FeatureToggle return 405 when the release-1a flag is disabled.
 */
@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.Release1AFeatureToggleIntegrationTest")
@DisplayName("Release 1A - returns 405 when release-1a flag is disabled")
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1a=false"
})
class Release1AFeatureToggleIntegrationTest extends AbstractFeatureToggleIntegrationTest {

    private static final String DRAFT_ACCOUNT_ID = "/draft-accounts/100000";

    static Stream<Arguments> release1aEndpoints() {
        return Stream.of(
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
            args("GET /results/{id}", withAuth(get("/results/FVS"))),
            args("GET /results", withAuth(get("/results")))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("release1aEndpoints")
    @DisplayName("should return 405 Method Not Allowed")
    void shouldReturn405WhenRelease1aIsDisabled(String description, MockHttpServletRequestBuilder request)
        throws Exception {
        log.debug("Testing feature-disabled 405 for: {}", description);
        mockMvc.perform(request)
            .andExpect(status().isMethodNotAllowed());
    }
}
