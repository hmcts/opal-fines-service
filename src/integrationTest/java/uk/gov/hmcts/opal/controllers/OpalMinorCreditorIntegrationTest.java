package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

@ActiveProfiles({"integration", "opal"})
@Sql(scripts = "classpath:db/insertData/insert_into_minor_creditors.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_minor_creditors.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.OpalMinorCreditorIntegrationTest")
public class OpalMinorCreditorIntegrationTest extends MinorCreditorControllerIntegrationTest {

    @Test
    void testPostSearchMinorCreditor_Success() throws Exception {
        super.postSearchMinorCreditorImpl_Success(log);
    }

    @Test
    void search_checkLetterReturnsBoth() throws Exception {
        super.search_checkLetter_returnsBoth(log);
    }

    @Test
    void search_NoCheckLetterReturnsBoth() throws Exception {
        super.search_noCheckLetter_returnsBoth(log);
    }

    @Test
    void search_noResultsForUnknownBusinessUnit_returnsEmpty() throws Exception {
        super.search_noResultsForUnknownBusinessUnit_returnsEmpty(log);
    }

    @Test
    void search_orgNamePrefix_normalizedMatches() throws Exception {
        super.search_orgNamePrefix_normalizedMatches(log);
    }

    @Test
    void search_accountNumber_withWildcardChars_treatedLiterally() throws Exception {
        super.search_accountNumber_withWildcardChars_treatedLiterally(log);
    }

    @Test
    void postSearch_missingAuthHeader_returns401ProblemJson() throws Exception {
        super.postSearch_missingAuthHeader_returns401();
    }

    @Test
    void postSearch_invalidToken_returns401ProblemJson() throws Exception {
        super.postSearch_invalidToken_returns401ProblemJson();
    }

    @Test
    void postSearch_authenticatedWithoutPermission_returns403ProblemJson() throws Exception {
        super.postSearch_authenticatedWithoutPermission_returns403ProblemJson();
    }

}

