package uk.gov.hmcts.opal.controllers;

import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Period;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

@ActiveProfiles({"integration", "opal"})
@Sql(
    scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS
)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql",
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS
)
@Slf4j
abstract class AbstractOpalDefendantsIntegrationTest extends AbstractIntegrationTest {

    protected static final String URL_BASE = "/defendant-accounts";
    protected static final String DEFENDANT_GLANCE_RESPONSE_SCHEMA = SchemaPaths.DEFENDANT_ACCOUNT
        + "/getDefendantAccountAtAGlanceResponse.json";
    protected static final String DEFENDANT_PARTY_RESPONSE_SCHEMA = SchemaPaths.DEFENDANT_ACCOUNT
        + "/getDefendantAccountPartyResponse.json";
    protected static final String DEFENDANT_FIXED_PENALTY_RESPONSE_SCHEMA =
        SchemaPaths.DEFENDANT_ACCOUNT + "/getDefendantAccountFixedPenaltyResponse.json";
    protected static final LocalDate ACCOUNT_77_BIRTH_DATE = LocalDate.of(1980, 2, 3);

    @MockitoBean
    protected UserStateService userStateService;

    @MockitoSpyBean
    protected JsonSchemaValidationService jsonSchemaValidationService;

    @MockitoBean
    protected UserState userState;

    @MockitoBean
    protected AccessTokenService accessTokenService;

    @BeforeEach
    void setupUserState() {
        Mockito.when(userState.anyBusinessUnitUserHasPermission(Mockito.any())).thenReturn(true);
        Mockito.when(userStateService.checkForAuthorisedUser(Mockito.any())).thenReturn(userState);
    }

    protected static String commentAndNotesPayload(String accountComment) {
        return commentAndNotesPayload(accountComment, null, null, null);
    }

    protected static String commentAndNotesPayload(String accountComment, String note1, String note2, String note3) {
        return """
            {
              "comment_and_notes": {
                "account_comment": %s,
                "free_text_note_1": %s,
                "free_text_note_2": %s,
                "free_text_note_3": %s
              }
            }
            """.formatted(jsonValue(accountComment), jsonValue(note1), jsonValue(note2), jsonValue(note3));
    }

    protected static String jsonValue(String s) {
        if (s == null) {
            return "null";
        }
        return "\"" + s.replace("\"", "\\\"") + "\"";
    }

    protected static String expectedAge() {
        return String.valueOf(Period.between(ACCOUNT_77_BIRTH_DATE, LocalDate.now()).getYears());
    }

    protected HttpHeaders authorisedHeaders(String bearerToken, String businessUnitId, String ifMatch) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.add("Business-Unit-Id", businessUnitId);
        if (ifMatch != null) {
            headers.add(HttpHeaders.IF_MATCH, ifMatch);
        }
        return headers;
    }

    protected Integer versionFor(long defendantAccountId) {
        return jdbcTemplate.queryForObject(
            "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
            Integer.class,
            defendantAccountId
        );
    }

    protected void authoriseAllPermissions() {
        when(userStateService.checkForAuthorisedUser(org.mockito.ArgumentMatchers.any()))
            .thenReturn(UserStateUtil.allPermissionsUser());
    }

    protected void authorise(short businessUnitId, FinesPermission permission) {
        when(userStateService.checkForAuthorisedUser(org.mockito.ArgumentMatchers.any()))
            .thenReturn(UserStateUtil.permissionUser(businessUnitId, permission));
    }
}
