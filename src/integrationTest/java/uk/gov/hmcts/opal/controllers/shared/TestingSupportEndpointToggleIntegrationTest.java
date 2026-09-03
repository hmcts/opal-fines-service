package uk.gov.hmcts.opal.controllers.shared;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {"opal.testing-support-endpoints.enabled=false"})
public class TestingSupportEndpointToggleIntegrationTest extends AbstractFeatureToggleIntegrationTest {
    static Stream<Arguments> testingSupportEndpoints() {
        return Stream.of(
            // TestingSupportController
            args("GET /testing-support/is-legacy-mode",
                withAuth(get("/testing-support/is-legacy-mode"))),
            args("GET /testing-support/launchdarkly/bool/{featureKey}",
                withAuth(get("/testing-support/launchdarkly/bool/example-key"))),
            args("GET /testing-support/launchdarkly/string/{featureKey}",
                withAuth(get("/testing-support/launchdarkly/string/example-key"))),
            args("GET /testing-support/token/parse",
                withAuth(get("/testing-support/token/parse"))),
            args("GET /testing-support/user-client/{userId}",
                withAuth(get("/testing-support/user-client/0"))),
            args("DELETE /testing-support/defendant-accounts/{defendantAccountId}",
                withAuth(delete("/testing-support/defendant-accounts/1"))),

            // CreateFineAccountsController
            args("POST /s2s/create-fine-accounts",
                withAuthAndJson(post("/s2s/create-fine-accounts").content("{}"))),

            // DebtorProfileSearchController
            args("POST /s2s/debtor-profiles",
                withAuthAndJson(post("/s2s/debtor-profiles").content("{}"))),

            // MinorCreditorHistoryFixtureController
            args("POST /testing-support/minor-creditor-history",
                withAuthAndJson(post("/testing-support/minor-creditor-history").content("{}"))),
            args("DELETE /testing-support/minor-creditor-history/{creditorAccountId}",
                withAuth(delete("/testing-support/minor-creditor-history/1"))),

            // PrintRequestController
            args("POST /api/print/enqueue-print-jobs",
                withAuthAndJson(post("/api/print/enqueue-print-jobs").content("[]"))),
            args("POST /api/print/generate-pdf",
                withAuthAndJson(post("/api/print/generate-pdf").content("{}"))),
            args("POST /api/print/process-pending-jobs",
                withAuth(post("/api/print/process-pending-jobs"))),

            // EnforcerController
            args("GET /enforcers/{id}", withAuth(get("/enforcers/1"))),
            args("POST /enforcers/search", withAuthAndJson(post("/enforcers/search")
                .content("{}"))),
            args("GET /enforcers", withAuth(get("/enforcers"))),

            // AmendmentController
            args("GET /amendments/{id}", withAuth(get("/amendments/1"))),
            args("POST /amendments/search", withAuthAndJson(post("/amendments/search")
                .content("{}"))),

            // BusinessUnitController
            args("POST /business-units/search", withAuthAndJson(post("/business-units/search")
                .content("{}"))),

            // DraftAccountController
            args("POST /draft-accounts/search",
                withAuthAndJson(post("/draft-accounts/search").content("{}"))),
            args("DELETE /draft-accounts/{id}",
                withAuth(delete("/draft-accounts/100000"))),

            // LocalJusticeAreaController
            args("POST /local-justice-areas/search",
                withAuthAndJson(post("/local-justice-areas/search").content("{}"))),

            // MajorCreditorController
            args("POST /major-creditors/search",
                withAuthAndJson(post("/major-creditors/search").content("{}"))),

            // MinorCreditorController
            args("DELETE /minor-creditor-accounts/{id}",
                withAuth(delete("/minor-creditor-accounts/1")))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("testingSupportEndpoints")
    @DisplayName("should return 404 Not Found?")
    @JiraStory("PO-6409")
    @JiraEpic("PO-8248")
    void shouldReturn404WhenTestingSupportIsDisabled(String description,
        MockHttpServletRequestBuilder request)
        throws Exception {
        log.debug("Testing feature-disabled 404 for: {}", description);
        mockMvc.perform(request)
            .andExpect(status().isNotFound());
    }
}
