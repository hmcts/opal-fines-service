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

    @Test
    void testAC1b_ActiveAccountsOnlyTrue() throws Exception {
        super.testAC1b_ActiveAccountsOnlyTrue(log);
    }

    @Test
    void testAC1b_ActiveAccountsOnlyFalse() throws Exception {
        super.testAC1b_ActiveAccountsOnlyFalse(log);
    }

    @Test
    void testAC1a_MultiParam_ForenamesAndSurname() throws Exception {
        super.testAC1a_MultiParam_ForenamesAndSurname(log);
    }

    @Test
    void testAC1a_MultiParam_PostcodeAndBusinessUnitAndAccountNumber() throws Exception {
        super.testAC1a_MultiParam_PostcodeAndAccountNumber(log);
    }

    @Test
    void testAC1a_MultiParam_OrganisationAndAddress() throws Exception {
        super.testAC1a_MultiParam_OrganisationAndAddress(log);
    }

    @Test
    void testAC1ai_BusinessUnitFiltering() throws Exception {
        super.testAC1ai_BusinessUnitFiltering(log);
    }

    @Test
    void testAC2a_ExactMatchSurnameEnabled() throws Exception {
        super.testAC2a_ExactMatchSurnameEnabled(log);
    }

    @Test
    void testAC2ai_ExactMatchSurnameDisabled() throws Exception {
        super.testAC2ai_ExactMatchSurnameDisabled(log);
    }

    @Test
    void testAC2b_ExactMatchForenamesEnabled() throws Exception {
        super.testAC2b_ExactMatchForenamesEnabled(log);
    }

    @Test
    void testAC2bi_ExactMatchForenamesDisabled() throws Exception {
        super.testAC2bi_ExactMatchForenamesDisabled(log);
    }

    @Test
    void testAC2c_AddressLine1StartsWith() throws Exception {
        super.testAC2c_AddressLine1StartsWith(log);
    }

    @Test
    void testAC2c_PostcodeStartsWith() throws Exception {
        super.testAC2c_PostcodeStartsWith(log);
    }

    // @Test
    // void testAC3a_CompanyNameExactMatch() throws Exception {
    //     super.testAC3a_CompanyNameExactMatch(log);
    // }

    // @Test
    // void testAC3ai_CompanyNameStartsWith() throws Exception {
    //     super.testAC3ai_CompanyNameStartsWith(log);
    // }

    // @Test
    // void testAC3ai_CompanyNameStartsWithPartial() throws Exception {
    //     super.testAC3ai_CompanyNameStartsWithPartial(log);
    // }
}

