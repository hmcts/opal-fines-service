package uk.gov.hmcts.opal.service.reference.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.exception.InvalidReferenceValidationException;

@Sql(scripts = "classpath:db/insertData/insert_draft_account_result_validation.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_draft_account_result_validation.sql", executionPhase = AFTER_TEST_METHOD)
class DraftAccountResultValidationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OpalDraftAccountReferenceValidationService opalService;

    @Autowired
    private LegacyDraftAccountReferenceValidationService legacyService;

    @ParameterizedTest
    @CsvSource({"DVOPEN,true", "DVLGEN,false"})
    void validatesEnforcementAgainstEachModesResults(String resultId, boolean opalValid) {
        String account = enforcementAccount(resultId);
        DraftAccountReferenceValidationService valid = opalValid ? opalService : legacyService;
        DraftAccountReferenceValidationService invalid = opalValid ? legacyService : opalService;
        assertDoesNotThrow(() -> valid.validateReferences((short) 920, account));
        assertFailure(invalid, account, "is not an enforcement result");
    }

    @ParameterizedTest
    @CsvSource({"DVOPIM,true", "DVLGIM,false"})
    void validatesImpositionAgainstEachModesResults(String resultId, boolean opalValid) {
        String account = impositionAccount(resultId, "");
        DraftAccountReferenceValidationService valid = opalValid ? opalService : legacyService;
        DraftAccountReferenceValidationService invalid = opalValid ? legacyService : opalService;
        assertDoesNotThrow(() -> valid.validateReferences((short) 920, account));
        assertFailure(invalid, account, "is not an imposition result");
    }

    private String impositionAccount(String resultId, String extra) {
        return "{\"offences\":[{\"impositions\":[{\"result_id\":\"" + resultId + "\"" + extra + "}]}]}";
    }

    private String enforcementAccount(String resultId) {
        return "{\"payment_terms\":{\"enforcements\":[{\"result_id\":\"" + resultId + "\"}]}}";
    }

    private void assertFailure(DraftAccountReferenceValidationService service, String account, String expected) {
        InvalidReferenceValidationException exception = assertThrows(InvalidReferenceValidationException.class,
            () -> service.validateReferences((short) 920, account));
        assertTrue(exception.getMessage().contains(expected), exception.getMessage());
    }
}
