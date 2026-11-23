package uk.gov.hmcts.opal.steps.defendantaccount;

import io.restassured.builder.ResponseBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import uk.gov.hmcts.opal.context.ScenarioContextHolder;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;
import uk.gov.hmcts.opal.utils.TestHttpClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class DefendantAccountAuditStepDefTest {

    private final RequestSpecification request = mock(RequestSpecification.class, RETURNS_SELF);
    private MockedStatic<SerenityRest> rest;
    private MockedStatic<BearerTokenStepDef> tokens;
    private MockedStatic<TestHttpClient> userStateClient;

    @BeforeEach
    void setUp() {
        rest = mockStatic(SerenityRest.class);
        tokens = mockStatic(BearerTokenStepDef.class);
        userStateClient = mockStatic(TestHttpClient.class);
        rest.when(SerenityRest::given).thenReturn(request);
        tokens.when(BearerTokenStepDef::getToken).thenReturn("test-token");
        ScenarioContextHolder.current().setCreatedDefendantAccountId("123");
    }

    @AfterEach
    void tearDown() {
        rest.close();
        tokens.close();
        userStateClient.close();
        ScenarioContextHolder.clear();
    }

    @ParameterizedTest
    @CsvSource({"false,L077JG,opal-test@dev.platform.hmcts.net", "true,L077OA,L077OA", "true,L077JG,L077JG",
        "false,L077OA,opal-test@dev.platform.hmcts.net"})
    void checksModeSpecificHistoryIdentity(boolean legacyMode, String businessUserId, String username)
        throws Exception {
        mode(legacyMode);
        userState(businessUserId);
        history("""
            {"historyItems":[{"type":"Amendment","postedDetails":{
              "posted_by":"%s","posted_by_name":"%s"}}]}
            """.formatted(businessUserId, username));

        new DefendantAccountAuditStepDef().createdDefendantAccountAmendmentHistoryIsRecordedForAuditTestUser();
    }

    @ParameterizedTest
    @CsvSource({"false,L077JG,opal-test@dev.platform.hmcts.net", "true,L077OA,L077OA", "true,L077JG,L077JG",
        "false,L077OA,opal-test@dev.platform.hmcts.net"})
    void checksModeSpecificPersistedAmendmentIdentity(boolean legacyMode, String businessUserId, String username)
        throws Exception {
        mode(legacyMode);
        userState(businessUserId);
        history("{\"historyItems\":[]}");
        when(request.post(anyString())).thenReturn(response("""
            {"searchData":[{"amended_by":"%s","amended_by_name":"%s"}]}
            """.formatted(businessUserId, username)));

        new DefendantAccountAuditStepDef().createdDefendantAccountAmendmentHistoryIsRecordedForAuditTestUser();
    }

    @ParameterizedTest
    @CsvSource({"false,L077JG,opal-test@dev.platform.hmcts.net", "true,L077OA,L077OA", "true,L077JG,L077JG",
        "false,L077OA,opal-test@dev.platform.hmcts.net"})
    void checksModeSpecificPaymentTermsIdentity(boolean legacyMode, String businessUserId, String username)
        throws Exception {
        mode(legacyMode);
        userState(businessUserId);
        rest.when(SerenityRest::lastResponse).thenReturn(paymentTerms(businessUserId, username));

        new DefendantAccountAuditStepDef().paymentTermsResponseMatchesTheDocumentedContract();
    }

    @ParameterizedTest
    @CsvSource({"false,L077OA", "true,L077JG"})
    void rejectsBusinessUserDifferentFromAuthenticatedUser(boolean legacyMode, String wrongBusinessUserId) {
        mode(legacyMode);
        userState(legacyMode ? "L077OA" : "L077JG");
        history("""
            {"historyItems":[{"type":"Amendment","postedDetails":{
              "posted_by":"%s","posted_by_name":"%s"}}]}
            """.formatted(wrongBusinessUserId, wrongBusinessUserId));
        rest.when(SerenityRest::lastResponse).thenReturn(paymentTerms(wrongBusinessUserId, wrongBusinessUserId));

        var steps = new DefendantAccountAuditStepDef();
        assertThatThrownBy(steps::createdDefendantAccountAmendmentHistoryIsRecordedForAuditTestUser)
            .isInstanceOf(AssertionError.class);
        assertThatThrownBy(steps::paymentTermsResponseMatchesTheDocumentedContract)
            .isInstanceOf(AssertionError.class).hasMessageContaining("Unexpected posted_by");
    }

    private void userState(String businessUserId) {
        userStateClient.when(() -> TestHttpClient.get(
            BaseStepDef.getTestUrl() + "/testing-support/user-client/0",
            Map.of("Accept", "*/*", "Authorization", "Bearer test-token")))
            .thenReturn(new TestHttpClient.TestHttpResponse(200, """
                {"business_unit_user":[
                  {"business_unit_id":73,"business_unit_user_id":"L073OTHER"},
                  {"business_unit_id":77,"business_unit_user_id":"%s"}]}
                """.formatted(businessUserId)));
    }

    private void mode(boolean legacyMode) {
        when(request.get(BaseStepDef.getTestUrl() + "/testing-support/is-legacy-mode"))
            .thenReturn(response(Boolean.toString(legacyMode)));
    }

    private void history(String body) {
        when(request.get(BaseStepDef.getTestUrl() + "/defendant-accounts/123/history?itemTypes=amendment"))
            .thenReturn(response(body));
    }

    private Response paymentTerms(String businessUserId, String username) {
        return response("""
            {"payment_terms":{"posted_details":{"posted_by":"%s","posted_by_name":"%s"}},
             "payment_card_last_requested":"2026-11-12"}
            """.formatted(businessUserId, username));
    }

    private Response response(String body) {
        return new ResponseBuilder().setStatusCode(200).setBody(body).build();
    }
}
