package uk.gov.hmcts.opal.controllers.shared;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.ADD_ACCOUNT_ACTIVITY_NOTES;
import static uk.gov.hmcts.opal.controllers.shared.util.UserStateUtil.allFinesPermissionsToken;
import static uk.gov.hmcts.opal.controllers.shared.util.UserStateUtil.permissionsToken;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

abstract class NotesIntegrationTest extends AbstractIntegrationTest {

    protected static final String URL_BASE = "/notes";

    @BeforeEach
    void setUp() {
        userStateStub.addPermissions((short) 78, ADD_ACCOUNT_ACTIVITY_NOTES);
    }


    @DisplayName("OPAL: POST /notes/add creates note for defendant account [PO-1566]")
    @JiraStory("PO-1566")
    void postNotesImpl(Logger log) throws Exception {
        // Arrange
        Note note = new Note();
        note.setNoteText("test");
        note.setRecordId("77");
        note.setRecordType(RecordType.DEFENDANT_ACCOUNTS);
        note.setNoteType("AA");

        AddNoteRequest request = new AddNoteRequest();
        request.setActivityNote(note);

        final String payload = objectMapper.writeValueAsString(request);
        log.info(":testPostNotes payload: {}", payload);

        // Read the current version immediately before use
        final Integer versionBefore = defendantAccountVersionFor(77L);
        assertThat(versionBefore).isNotNull();
        // Act
        ResultActions result =
            mockMvc.perform(
                post(URL_BASE + "/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .header("authorization", userStateStub.getBearerToken())
                    .header(HttpHeaders.IF_MATCH, "\"" + versionBefore + "\"")
                    .header("Business-Unit-Id", "78")
                    .with(authentication(allFinesPermissionsToken()))
            );

        // Assert
        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":testPostNotes response:\n{}", ToJsonString.toPrettyJson(body));
        result.andExpect(status().isCreated());

        // Assert defendantAccount version incremented
        final Integer versionAfter = defendantAccountVersionFor(77L);
        assertThat(versionAfter).isEqualTo(versionBefore + 1);
    }

    @DisplayName("post notes for a defendant account ID that does not exist [PO-1566]")
    @JiraStory("PO-1566")
    void postNotes_IDNotFoundError(Logger log) throws Exception {

        Note note = new Note();
        note.setNoteText("test");
        note.setRecordId("7");
        note.setRecordType(RecordType.DEFENDANT_ACCOUNTS);
        note.setNoteType("AA");

        AddNoteRequest request = new AddNoteRequest();

        request.setActivityNote(note);

        ResultActions resultActions =
            mockMvc.perform(
                post(URL_BASE + "/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .header("authorization", userStateStub.getBearerToken())
                    .header("If-Match", "1")
                    .header("Business-Unit-Id", "78")
                    .with(authentication(allFinesPermissionsToken()))
            );

        String body = resultActions.andReturn().getResponse().getContentAsString();

        log.info(":testPostNotes: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isNotFound());
    }

    @DisplayName("post notes for an invalid defendant account ID [PO-1566]")
    @JiraStory("PO-1566")
    void postNotes_badRequest(Logger log) throws Exception {

        Note note = new Note();
        note.setRecordId("ABC1");
        note.setRecordType(RecordType.CREDITOR_ACCOUNTS);

        AddNoteRequest request = new AddNoteRequest();

        request.setActivityNote(note);

        ResultActions resultActions =
            mockMvc.perform(
                post(URL_BASE + "/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .header("authorization", userStateStub.getBearerToken())
                    .header("If-Match", "1")
                    .header("Business-Unit-Id", "78")
                    .with(authentication(allFinesPermissionsToken()))
            );

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testPostNotes: Response body:\n" + ToJsonString.toPrettyJson(body));
        resultActions.andExpect(status().isBadRequest());
    }

    @DisplayName("post notes - user without permission [PO-1566]")
    @JiraStory("PO-1566")
    void postNotes_UserWithoutPermission(FinesPermission permission) throws Exception {

        Note note = new Note();
        note.setNoteText("test note");
        note.setRecordId("77");
        note.setRecordType(RecordType.DEFENDANT_ACCOUNTS);
        note.setNoteType("AA");

        AddNoteRequest request = new AddNoteRequest();
        request.setActivityNote(note);

        ResultActions resultActions = mockMvc.perform(
            post(URL_BASE + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("If-Match", "1")
                .header("Business-Unit-Id", "78")
                .with(authentication(permissionsToken((short) 78, permission)))
        );
        resultActions.andExpect(status().isForbidden())
            .andExpect(jsonPath("$.detail")
                .value("You do not have permission to access this resource"));
    }

    protected static Stream<FinesPermission> nonNotesPermissions() {
        return Stream.of(FinesPermission.values())
            .filter(permission -> permission != ADD_ACCOUNT_ACTIVITY_NOTES);
    }

    void postNotes_BusinessUnitAuthorization(BusinessUnitAuthorizationScenario scenario) throws Exception {

        userStateStub.setupWithNoPermissions();

        if (scenario.authenticationBusinessUnitId() != null) {
            userStateStub.addPermissions(scenario.authenticationBusinessUnitId(), scenario.permission());
        }

        Note note = new Note();
        note.setNoteText("test note");
        note.setRecordId("77");
        note.setRecordType(RecordType.DEFENDANT_ACCOUNTS);
        note.setNoteType("AA");

        AddNoteRequest request = new AddNoteRequest();
        request.setActivityNote(note);

        var requestBuilder =
            post(URL_BASE + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken())
                .header(HttpHeaders.IF_MATCH, "1")
                .with(authentication(permissionsToken(
                    scenario.authenticationBusinessUnitId(), scenario.permission())));

        if (scenario.requestBusinessUnitId() != null) {
            requestBuilder.header("Business-Unit-Id", scenario.requestBusinessUnitId());
        }

        ResultActions resultActions = mockMvc.perform(requestBuilder);

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testPostNotes_BusinessUnitAuthorization [{}] response body:\n{}",
            scenario.displayName(), ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().is(scenario.expectedStatus().value()))
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value(scenario.expectedType()))
            .andExpect(jsonPath("$.title").value(scenario.expectedTitle()))
            .andExpect(jsonPath("$.detail").value(scenario.expectedDetail()))
            .andExpect(jsonPath("$.status").value(scenario.expectedStatus().value()))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    protected static Stream<BusinessUnitAuthorizationScenario> businessUnitAuthorizationScenarios() {
        return Stream.of(
            new BusinessUnitAuthorizationScenario(
                "missing Business-Unit-Id header",
                null,
                (short) 78,
                ADD_ACCOUNT_ACTIVITY_NOTES,
                HttpStatus.BAD_REQUEST,
                "https://hmcts.gov.uk/problems/missing-header",
                "Missing Required Header",
                "Required request header \"Business-Unit-Id\" is missing"
            ),
            new BusinessUnitAuthorizationScenario(
                "wrong business unit in header",
                (short) 99,
                (short) 78,
                ADD_ACCOUNT_ACTIVITY_NOTES,
                HttpStatus.FORBIDDEN,
                "https://hmcts.gov.uk/problems/forbidden",
                "Forbidden",
                "You do not have permission to access this resource"
            ),
            new BusinessUnitAuthorizationScenario(
                "permission present in another BU",
                (short) 78,
                (short) 99,
                ADD_ACCOUNT_ACTIVITY_NOTES,
                HttpStatus.FORBIDDEN,
                "https://hmcts.gov.uk/problems/forbidden",
                "Forbidden",
                "You do not have permission to access this resource"
            )
        );
    }

    protected record BusinessUnitAuthorizationScenario(
        String displayName,
        Short requestBusinessUnitId,
        Short authenticationBusinessUnitId,
        FinesPermission permission,
        HttpStatus expectedStatus,
        String expectedType,
        String expectedTitle,
        String expectedDetail
    ) {

        @Override
        public String toString() {
            return displayName;
        }
    }

    @DisplayName("post notes for a defendant account in legacy [PO-1975]")
    @JiraStory("PO-1975")
    void legacyTestAddNoteSuccess(Logger log) throws Exception {

        userStateStub.addPermissions((short) 78, ADD_ACCOUNT_ACTIVITY_NOTES);

        Note note = new Note();
        note.setNoteText("test");
        note.setRecordId("77");
        note.setRecordType(RecordType.DEFENDANT_ACCOUNTS);
        note.setNoteType("AA");

        AddNoteRequest request = new AddNoteRequest();

        request.setActivityNote(note);

        ResultActions resultActions =
            mockMvc.perform(
                post(URL_BASE + "/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .header("authorization", userStateStub.getBearerToken())
                    .header("If-Match", "1")
                    .header("Business-Unit-Id", 78)
                    .with(authentication(allFinesPermissionsToken()))
            );

        String body = resultActions.andReturn().getResponse().getContentAsString();

        log.info(":testPostNotes: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isCreated());
    }

    @DisplayName("post notes for a defendant account ID that does not exist in legacy [PO-1975]")
    @JiraStory("PO-1975")
    void legacyTestAddNote500Error(Logger log) throws Exception {

        userStateStub.addPermissions((short) 78, ADD_ACCOUNT_ACTIVITY_NOTES);

        Note note = new Note();
        note.setNoteText("FAIL");
        note.setRecordId("77");
        note.setRecordType(RecordType.DEFENDANT_ACCOUNTS);
        note.setNoteType("AA");

        AddNoteRequest request = new AddNoteRequest();

        request.setActivityNote(note);

        ResultActions resultActions =
            mockMvc.perform(
                post(URL_BASE + "/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .header("authorization", userStateStub.getBearerToken())
                    .header("If-Match", "5")
                    .header("Business-Unit-Id", "78")
                    .with(authentication(allFinesPermissionsToken()))
            );

        String body = resultActions.andReturn().getResponse().getContentAsString();

        log.info(":testPostNotes: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().is5xxServerError());

    }

    @DisplayName("post notes for a legacy-only defendant account [PO-10341]")
    @JiraStory("PO-10341")
    void legacyOnlyAccountAddNoteSuccess(Logger log) throws Exception {

        userStateStub.addPermissions((short) 77, ADD_ACCOUNT_ACTIVITY_NOTES);

        Note note = new Note();
        note.setNoteText("legacy-only account note");
        note.setRecordId("770000004141");
        note.setRecordType(RecordType.DEFENDANT_ACCOUNTS);
        note.setNoteType("AA");

        AddNoteRequest request = new AddNoteRequest();
        request.setActivityNote(note);

        ResultActions resultActions = mockMvc.perform(
            post(URL_BASE + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken())
                .header(HttpHeaders.IF_MATCH, OVER_LONG_VERSION_ETAG)
                .header("Business-Unit-Id", 77)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
        );

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":legacyOnlyAccountAddNoteSuccess: Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isCreated());

        WireMock.verify(1, postRequestedFor(urlPathEqualTo("/opal"))
            .withQueryParam("actionType", equalTo("addNote"))
            .withRequestBody(matchingJsonPath("$.business_unit_id", equalTo("77")))
            .withRequestBody(matchingJsonPath("$.business_unit_user_id"))
            .withRequestBody(matchingJsonPath("$.activity_note.record_id", equalTo("770000004141")))
            .withRequestBody(matchingJsonPath("$.activity_note.record_type", equalTo("defendant_accounts")))
            .withRequestBody(matchingJsonPath("$.activity_note.note_text", equalTo("legacy-only account note")))
            .withRequestBody(matchingJsonPath("$.activity_note.note_type", equalTo("AA"))));
    }

    @DisplayName("post notes then fetch header summary for a legacy-only defendant account [PO-10341]")
    @JiraStory("PO-10341")
    void legacyOnlyAccountAddNoteThenFetchHeaderSummary(Logger log) throws Exception {

        userStateStub.addPermissions((short) 77, ADD_ACCOUNT_ACTIVITY_NOTES);
        userStateStub.addPermissions((short) 77, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);

        stubFor(WireMock.post(urlPathEqualTo("/opal"))
            .withQueryParam("actionType", equalTo("getDefendantAccountHeaderSummary"))
            .withRequestBody(matchingJsonPath("$.defendant_account_id", equalTo("770000004141")))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/xml")
                .withBody(legacyOnlyHeaderSummaryXml())));

        legacyOnlyAccountAddNoteSuccess(log);

        ResultActions resultActions = mockMvc.perform(
            get("/defendant-accounts/770000004141/header-summary")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken())
                .accept(MediaType.APPLICATION_JSON)
        );

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":legacyOnlyAccountAddNoteThenFetchHeaderSummary: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_id").value("770000004141"))
            .andExpect(jsonPath("$.account_number").value("770000004141A"))
            .andExpect(jsonPath("$.business_unit_summary.business_unit_id").value(77));

        WireMock.verify(1, postRequestedFor(urlPathEqualTo("/opal"))
            .withQueryParam("actionType", equalTo("getDefendantAccountHeaderSummary"))
            .withRequestBody(matchingJsonPath("$.defendant_account_id", equalTo("770000004141"))));
    }

    @DisplayName("post notes then fetch history for a legacy-only defendant account [PO-10341]")
    @JiraStory("PO-10341")
    void legacyOnlyAccountAddNoteThenFetchHistory(Logger log) throws Exception {

        userStateStub.addPermissions((short) 77, ADD_ACCOUNT_ACTIVITY_NOTES);
        userStateStub.addPermissions((short) 77, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);

        stubFor(WireMock.post(urlPathEqualTo("/opal"))
            .withQueryParam("actionType", equalTo("getDefendantAccountHistory"))
            .withRequestBody(matchingJsonPath("$.defendant_account_id", equalTo("770000004141")))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/xml")
                .withBody(legacyOnlyHistoryXml())));

        legacyOnlyAccountAddNoteSuccess(log);

        ResultActions resultActions = mockMvc.perform(
            get("/defendant-accounts/770000004141/history")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken())
                .accept(MediaType.APPLICATION_JSON)
        );

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":legacyOnlyAccountAddNoteThenFetchHistory: Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.historyItems[0].type").value("Note"))
            .andExpect(jsonPath("$.historyItems[0].details.noteText").value("legacy-only account note"));

        WireMock.verify(1, postRequestedFor(urlPathEqualTo("/opal"))
            .withQueryParam("actionType", equalTo("getDefendantAccountHistory"))
            .withRequestBody(matchingJsonPath("$.defendant_account_id", equalTo("770000004141"))));
    }

    private static String legacyOnlyHeaderSummaryXml() {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <response>
              <version>9223372036854775808</version>
              <defendant_account_id>770000004141</defendant_account_id>
              <account_number>770000004141A</account_number>
              <defendant_party_id>7</defendant_party_id>
              <debtor_type>Defendant</debtor_type>
              <is_youth>false</is_youth>
              <account_status_reference>
                <account_status_code>L</account_status_code>
              </account_status_reference>
              <account_type>Fine</account_type>
              <business_unit_summary>
                <business_unit_id>77</business_unit_id>
                <business_unit_name>Legacy Unit</business_unit_name>
                <business_unit_code>77</business_unit_code>
                <welsh_speaking>N</welsh_speaking>
              </business_unit_summary>
              <payment_state_summary>
                <imposed_amount>700.58</imposed_amount>
                <arrears_amount>300.00</arrears_amount>
                <paid_amount>200.00</paid_amount>
                <account_balance>5000.00</account_balance>
              </payment_state_summary>
              <has_consolidated_accounts>false</has_consolidated_accounts>
            </response>
            """;
    }

    private static String legacyOnlyHistoryXml() {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <response>
              <version>9223372036854775808</version>
              <history_items>
                <history_items_element>
                  <posted_details>
                    <posted_date>2026-09-03T09:58:34</posted_date>
                    <posted_by>01000000A</posted_by>
                    <posted_by_name>Opal Test User</posted_by_name>
                  </posted_details>
                  <type>Note</type>
                  <details>
                    <note_text>legacy-only account note</note_text>
                  </details>
                </history_items_element>
              </history_items>
            </response>
            """;
    }
}
