package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import uk.gov.hmcts.opal.AbstractIntegrationWithSecurityTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.controllers.util.DefendantAccountVersionUtil;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

@Slf4j
abstract class AbstractLegacyDefendantsIntegrationTest extends AbstractIntegrationWithSecurityTest {

    protected static final String URL_BASE = "/defendant-accounts";
    protected static final String DEFENDANT_PARTY_RESPONSE_SCHEMA = SchemaPaths.DEFENDANT_ACCOUNT
        + "/getDefendantAccountPartyResponse.json";

    @MockitoSpyBean
    protected JsonSchemaValidationService jsonSchemaValidationService;

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
        return DefendantAccountVersionUtil.getVersion(jdbcTemplate, defendantAccountId);
    }
}
