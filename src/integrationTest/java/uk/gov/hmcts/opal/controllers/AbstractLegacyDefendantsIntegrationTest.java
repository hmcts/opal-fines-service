package uk.gov.hmcts.opal.controllers;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

@Slf4j
abstract class AbstractLegacyDefendantsIntegrationTest extends AbstractIntegrationTest {

    protected static final String URL_BASE = "/defendant-accounts";
    protected static final String DEFENDANT_PARTY_RESPONSE_SCHEMA = SchemaPaths.DEFENDANT_ACCOUNT
        + "/getDefendantAccountPartyResponse.json";

    @MockitoBean
    protected UserStateService userStateService;

    @MockitoBean
    protected UserStateClientService userStateClientService;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

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
        Mockito.when(userStateClientService.getUserStateByAuthenticatedUser()).thenReturn(Optional.of(userState));
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

    protected static String jsonValue(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value.replace("\"", "\\\"") + "\"";
    }

    protected Integer versionFor(long defendantAccountId) {
        return jdbcTemplate.queryForObject(
            "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
            Integer.class,
            defendantAccountId
        );
    }
}
