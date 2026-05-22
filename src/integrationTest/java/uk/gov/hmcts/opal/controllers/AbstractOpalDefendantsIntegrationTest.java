package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.support.UserServiceStub.stubUserWithAllPermissions;
import static uk.gov.hmcts.opal.support.UserServiceStub.stubUserWithPermission;

import java.time.LocalDate;
import java.time.Period;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationWithSecurityTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.controllers.util.DefendantAccountVersionUtil;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

@ActiveProfiles(profiles = {"integration-with-spring-security", "opal"}, inheritProfiles = false)
@Sql(
    scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS
)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql",
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS
)
@Slf4j
abstract class AbstractOpalDefendantsIntegrationTest extends AbstractIntegrationWithSecurityTest {

    protected static final String URL_BASE = "/defendant-accounts";
    protected static final String DEFENDANT_GLANCE_RESPONSE_SCHEMA = SchemaPaths.DEFENDANT_ACCOUNT
        + "/getDefendantAccountAtAGlanceResponse.json";
    protected static final String DEFENDANT_PARTY_RESPONSE_SCHEMA = SchemaPaths.DEFENDANT_ACCOUNT
        + "/getDefendantAccountPartyResponse.json";
    protected static final String DEFENDANT_FIXED_PENALTY_RESPONSE_SCHEMA =
        SchemaPaths.DEFENDANT_ACCOUNT + "/getDefendantAccountFixedPenaltyResponse.json";
    protected static final LocalDate ACCOUNT_77_BIRTH_DATE = LocalDate.of(1980, 2, 3);

    @MockitoSpyBean
    protected JsonSchemaValidationService jsonSchemaValidationService;

    protected static @NonNull HttpHeaders buildHttpHeaders(String number, String currentVersion) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(validToken);
        headers.add("Business-Unit-Id", number);
        headers.add(HttpHeaders.IF_MATCH, currentVersion);
        return headers;
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
        return DefendantAccountVersionUtil.getVersion(jdbcTemplate, defendantAccountId);
    }

    protected void authoriseAllPermissions() {
        stubUserWithAllPermissions(78);
    }

    protected void authorise(short businessUnitId, FinesPermission permission) {
        stubUserWithPermission(businessUnitId, permission);
    }
}
