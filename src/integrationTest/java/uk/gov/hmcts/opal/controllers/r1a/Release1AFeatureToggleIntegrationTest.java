package uk.gov.hmcts.opal.controllers.r1a;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.opal.controllers.shared.AbstractFeatureToggleIntegrationTest;
import uk.gov.hmcts.opal.controllers.shared.util.DraftAccountTestData;
import uk.gov.hmcts.opal.common.user.authentication.exception.CommonGlobalExceptionHandler;
import uk.gov.hmcts.opal.controllers.BusinessUnitController;
import uk.gov.hmcts.opal.controllers.CourtController;
import uk.gov.hmcts.opal.controllers.DraftAccountController;
import uk.gov.hmcts.opal.controllers.FeatureToggleWebMvcTestConfiguration;
import uk.gov.hmcts.opal.controllers.LocalJusticeAreaController;
import uk.gov.hmcts.opal.controllers.MajorCreditorController;
import uk.gov.hmcts.opal.controllers.OffenceController;
import uk.gov.hmcts.opal.controllers.ProscutorController;
import uk.gov.hmcts.opal.controllers.ResultsApiController;
import uk.gov.hmcts.opal.service.DraftAccountService;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.BusinessUnitService;
import uk.gov.hmcts.opal.service.opal.CourtService;
import uk.gov.hmcts.opal.service.opal.LocalJusticeAreaService;
import uk.gov.hmcts.opal.service.opal.MajorCreditorService;
import uk.gov.hmcts.opal.service.opal.OffenceService;
import uk.gov.hmcts.opal.service.opal.ProsecutorService;
import uk.gov.hmcts.opal.service.opal.ResultService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;


/**
 * Verifies that all Release 1A endpoints guarded by @FeatureToggle return 404 when the release-1a flag is disabled.
 */
@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.Release1AFeatureToggleIntegrationTest")
@DisplayName("Release 1A - returns 404 when release-1a flag is disabled")
@Tag("ExtendedTest")
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1a=false"
})
@WebMvcTest(
    controllers = {BusinessUnitController.class, CourtController.class, DraftAccountController.class,
        LocalJusticeAreaController.class, MajorCreditorController.class, OffenceController.class,
        ProscutorController.class, ResultsApiController.class},
    excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CommonGlobalExceptionHandler.class)
)
@Import({FeatureToggleWebMvcTestConfiguration.class, BusinessUnitController.class, CourtController.class,
    DraftAccountController.class, LocalJusticeAreaController.class, MajorCreditorController.class,
    OffenceController.class, ProscutorController.class, ResultsApiController.class})
class Release1AFeatureToggleIntegrationTest extends AbstractFeatureToggleIntegrationTest {

    @MockitoBean private BusinessUnitService businessUnitService;
    @MockitoBean private UserStateService userStateService;
    @MockitoBean private CourtService courtService;
    @MockitoBean private DraftAccountService draftAccountService;
    @MockitoBean private LocalJusticeAreaService localJusticeAreaService;
    @MockitoBean private MajorCreditorService majorCreditorService;
    @MockitoBean private OffenceService offenceService;
    @MockitoBean private ProsecutorService prosecutorService;
    @MockitoBean private ResultService resultService;

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

    @ParameterizedTest(name = "{0}")
    @MethodSource("release1aEndpoints")
    @DisplayName("should return 404 Not Found")
    @JiraStory("PO-2833")
    @JiraEpic("PO-2352")
    @JiraTestKey("PO-6232")
    @JiraTestKey(value = "PO-8498", name = "\"GET /business-units/{id}\"")
    @JiraTestKey(value = "PO-8499", name = "\"GET /business-units\"")
    @JiraTestKey(value = "PO-8500", name = "\"GET /courts/{id}\"")
    @JiraTestKey(value = "PO-8501", name = "\"POST /courts/search\"")
    @JiraTestKey(value = "PO-8502", name = "\"GET /courts\"")
    @JiraTestKey(value = "PO-8503", name = "\"GET /draft-accounts/{id}\"")
    @JiraTestKey(value = "PO-8504", name = "\"GET /draft-accounts\"")
    @JiraTestKey(value = "PO-8505", name = "\"POST /draft-accounts\"")
    @JiraTestKey(value = "PO-8506", name = "\"PUT /draft-accounts/{id}\"")
    @JiraTestKey(value = "PO-8507", name = "\"PATCH /draft-accounts/{id}\"")
    @JiraTestKey(value = "PO-8508", name = "\"GET /local-justice-areas/{id}\"")
    @JiraTestKey(value = "PO-8509", name = "\"GET /local-justice-areas\"")
    @JiraTestKey(value = "PO-8510", name = "\"GET /major-creditors/{id}\"")
    @JiraTestKey(value = "PO-8511", name = "\"GET /major-creditors\"")
    @JiraTestKey(value = "PO-8512", name = "\"GET /offences/{id}\"")
    @JiraTestKey(value = "PO-8513", name = "\"POST /offences/search\"")
    @JiraTestKey(value = "PO-8514", name = "\"GET /offences\"")
    @JiraTestKey(value = "PO-8515", name = "\"GET /prosecutors/{id}\"")
    @JiraTestKey(value = "PO-8516", name = "\"GET /prosecutors\"")
    @JiraTestKey(value = "PO-8517", name = "\"GET /results\"")
    void shouldReturn404WhenRelease1aIsDisabled(String description, MockHttpServletRequestBuilder request)
        throws Exception {
        log.debug("Testing feature-disabled 404 for: {}", description);
        mockMvc.perform(request)
            .andExpect(status().isNotFound());
    }
}
