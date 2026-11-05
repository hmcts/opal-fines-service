package uk.gov.hmcts.opal.controllers.r1c;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.opal.controllers.shared.AbstractFeatureToggleIntegrationTest;
import uk.gov.hmcts.opal.common.user.authentication.exception.CommonGlobalExceptionHandler;
import uk.gov.hmcts.opal.controllers.DefendantAccountApiController;
import uk.gov.hmcts.opal.controllers.FeatureToggleWebMvcTestConfiguration;
import uk.gov.hmcts.opal.controllers.OutstandingAutoPaymentController;
import uk.gov.hmcts.opal.controllers.TestingSupportController;
import uk.gov.hmcts.opal.controllers.till.TillsApiController;
import uk.gov.hmcts.opal.mapper.history.DefendantAccountHistoryResponseMapper;
import uk.gov.hmcts.opal.mapper.request.ReplacePartyRequestMapper;
import uk.gov.hmcts.opal.service.DefendantAccountEnforcementService;
import uk.gov.hmcts.opal.service.DefendantAccountFixedPenaltyService;
import uk.gov.hmcts.opal.service.DefendantAccountPartyService;
import uk.gov.hmcts.opal.service.DefendantAccountPaymentTermsService;
import uk.gov.hmcts.opal.service.DefendantAccountService;
import uk.gov.hmcts.opal.service.DraftAccountService;
import uk.gov.hmcts.opal.service.ImpositionService;
import uk.gov.hmcts.opal.service.opal.BusinessUnitService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.InterfaceJobService;
import uk.gov.hmcts.opal.service.opal.LocalJusticeAreaService;
import uk.gov.hmcts.opal.service.opal.MajorCreditorService;
import uk.gov.hmcts.opal.service.opal.OpalCreditorAccountService;
import uk.gov.hmcts.opal.service.opal.OutstandingAutoPaymentService;
import uk.gov.hmcts.opal.service.opal.till.CreateTillService;
import uk.gov.hmcts.opal.service.opal.till.TillAllocationService;
import uk.gov.hmcts.opal.service.opal.till.TillSearchService;
import uk.gov.hmcts.opal.service.opal.till.TillsService;
import uk.gov.hmcts.opal.service.report.ReportInstanceDeletionService;
import uk.gov.hmcts.opal.service.opal.till.TestingSupportTillService;
import uk.gov.hmcts.opal.service.opal.DefendantAccountDeletionService;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.common.user.authorisation.client.mapper.UserStateMapper;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

/**
 * Verifies that all Release 1C Payment endpoints guarded by @FeatureToggle return 404 when the release-1c-payment
 * flag is disabled.
 */
@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.Release1CPaymentFeatureToggleIntegrationTest")
@DisplayName("Release 1C Payment - returns 404 when flag is disabled")
@Tag("ExtendedTest")
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1c-payment=false"
})
@WebMvcTest(controllers = {OutstandingAutoPaymentController.class, TestingSupportController.class,
    DefendantAccountApiController.class, TillsApiController.class},
    excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CommonGlobalExceptionHandler.class))
@Import({FeatureToggleWebMvcTestConfiguration.class, OutstandingAutoPaymentController.class,
    TestingSupportController.class, DefendantAccountApiController.class, TillsApiController.class})
class Release1CPaymentFeatureToggleIntegrationTest extends AbstractFeatureToggleIntegrationTest {

    @MockitoBean private OutstandingAutoPaymentService outstandingAutoPaymentService;
    @MockitoBean private DynamicConfigService dynamicConfigService;
    @MockitoBean private AccessTokenService accessTokenService;
    @MockitoBean private DefendantAccountDeletionService defendantAccountDeletionService;
    @MockitoBean private InterfaceJobService interfaceJobService;
    @MockitoBean private UserStateClientService userStateClientService;
    @MockitoBean private UserStateMapper userStateMapper;
    @MockitoBean private MajorCreditorService majorCreditorService;
    @MockitoBean private BusinessUnitService businessUnitService;
    @MockitoBean private OpalCreditorAccountService opalCreditorAccountService;
    @MockitoBean private DraftAccountService draftAccountService;
    @MockitoBean private LocalJusticeAreaService localJusticeAreaService;
    @MockitoBean private ReportInstanceDeletionService reportInstanceDeletionService;
    @MockitoBean private TestingSupportTillService testingSupportTillService;
    @MockitoBean private DefendantAccountService defendantAccountService;
    @MockitoBean private DefendantAccountEnforcementService defendantAccountEnforcementService;
    @MockitoBean private DefendantAccountHistoryResponseMapper defendantAccountHistoryResponseMapper;
    @MockitoBean private ImpositionService impositionService;
    @MockitoBean private DefendantAccountPartyService defendantAccountPartyService;
    @MockitoBean private DefendantAccountFixedPenaltyService defendantAccountFixedPenaltyService;
    @MockitoBean private DefendantAccountPaymentTermsService defendantAccountPaymentTermsService;
    @MockitoBean private ReplacePartyRequestMapper replacePartyRequestMapper;
    @MockitoBean private TillSearchService tillSearchService;
    @MockitoBean private TillsService tillsService;
    @MockitoBean private CreateTillService createTillService;
    @MockitoBean private TillAllocationService tillAllocationService;

    private static final String OUTSTANDING_AUTO_PAYMENT_PATH = "/business-units/outstanding-auto-payment-count";
    private static final String INTERFACE_JOBS_PATH = "/testing-support/interface-jobs";
    private static final String COURT_FEES_PATH = "/court-fees";
    private static final String DEFENDANT_ACCOUNTS_MASTER_PATH = "/defendant-accounts/%d/master";
    private static final String TILLS_PATH = "/tills";
    private static final String VALID_TILLS_REQUEST = """
        {
          "business_unit_id": 3630,
          "payments_in": [
            {
              "payment_details": {
                "amount": 12.34,
                "method": "Notes & Coins",
                "destination_type": "Fines",
                "allocation_type": "FULL",
                "additional_information": "Defendant"
              }
            }
          ]
        }
        """;

    static Stream<Arguments> release1cPaymentEndpoints() {
        return Stream.of(
            endpoint("GET " + OUTSTANDING_AUTO_PAYMENT_PATH, get(OUTSTANDING_AUTO_PAYMENT_PATH)),
            endpoint("DELETE " + INTERFACE_JOBS_PATH, delete(INTERFACE_JOBS_PATH).queryParam("ids", "1")),
            endpoint("GET " + COURT_FEES_PATH, get(COURT_FEES_PATH).queryParam("business_unit_id", "953")),
            endpoint("DELETE " + INTERFACE_JOBS_PATH, delete(INTERFACE_JOBS_PATH).queryParam("ids", "1")),
            endpoint("GET " + DEFENDANT_ACCOUNTS_MASTER_PATH, get(DEFENDANT_ACCOUNTS_MASTER_PATH.formatted(1))),
            endpoint("DELETE " + INTERFACE_JOBS_PATH, delete(INTERFACE_JOBS_PATH).queryParam("ids", "1")),
            args("POST " + TILLS_PATH, withAuthAndJson(post(TILLS_PATH)
                .content(VALID_TILLS_REQUEST))
                .accept(MediaType.valueOf("application/json+problem"))));
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
