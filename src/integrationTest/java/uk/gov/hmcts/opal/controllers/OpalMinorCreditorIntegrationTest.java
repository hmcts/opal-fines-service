package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.CreditorTransactionEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.CreditorTransactionRepository;
import uk.gov.hmcts.opal.repository.ImpositionRepository;
import uk.gov.hmcts.opal.repository.PartyRepository;
import uk.gov.hmcts.opal.repository.jpa.CreditorTransactionSpecs;
import uk.gov.hmcts.opal.repository.jpa.ImpositionSpecs;

@ActiveProfiles({"integration", "opal"})
@Sql(scripts = "classpath:db/insertData/insert_into_minor_creditors.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_minor_creditors.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.OpalMinorCreditorIntegrationTest")
public class OpalMinorCreditorIntegrationTest extends MinorCreditorControllerIntegrationTest {

    @MockitoSpyBean
    private ImpositionRepository impositionRepository;

    @MockitoSpyBean
    private CreditorTransactionRepository creditorTransactionRepository;

    @MockitoSpyBean
    private PartyRepository partyRepository;

    @MockitoSpyBean
    private CreditorAccountRepository creditorAccountRepository;

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

    @Test
    void testAC3a_CompanyNameExactMatch() throws Exception {
        super.testAC3a_CompanyNameExactMatch(log);
    }

    @Test
    void testAC3ai_CompanyNameStartsWith() throws Exception {
        super.testAC3ai_CompanyNameStartsWith(log);
    }

    @Test
    void testAC3ai_CompanyNameStartsWithPartial() throws Exception {
        super.testAC3ai_CompanyNameStartsWithPartial(log);
    }

    @Test
    void testAC3b_CompanyAddressLine1StartsWith() throws Exception {
        super.testAC3b_CompanyAddressLine1StartsWith(log);
    }

    @Test
    void testAC3b_CompanyPostcodeStartsWith() throws Exception {
        super.testAC3b_CompanyPostcodeStartsWith(log);
    }

    @Test
    void testAC3b_CompanyAddressAndPostcodeCombined() throws Exception {
        super.testAC3b_CompanyAddressAndPostcodeCombined(log);
    }

    @Test
    void getHeaderSummary_success() throws Exception {
        super.getHeaderSummaryImpl_Success(log);
    }

    @Test
    void getHeaderSummary_notFound_returns404() throws Exception {
        super.getHeaderSummary_notFound_returns404(log);
    }

    @Test
    void getHeaderSummary_missingAuthHeader_returns401() throws Exception {
        super.getHeaderSummary_missingAuthHeader_returns401();
    }

    @Test
    void getHeaderSummary_authenticatedWithoutPermission_returns403() throws Exception {
        super.getHeaderSummary_authenticatedWithoutPermission_returns403();
    }

    @Test
    void getHeaderSummary_timeout_returns408() throws Exception {
        super.getHeaderSummary_timeout_returns408(log);
    }

    @Test
    void getHeaderSummary_serviceUnavailable_returns503() throws Exception {
        super.getHeaderSummary_serviceUnavailable_returns503(log);
    }

    @Test
    void getHeaderSummary_serverError_returns500() throws Exception {
        super.getHeaderSummary_serverError_returns500(log);
    }

    @Test
    void deleteMinorCreditorAccount() throws Exception {
        // Arrange
        final Long creditorAccountId = 606L;
        final Long partyId = 9007L;
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        Specification<ImpositionEntity.Lite> impositionSpec = ImpositionSpecs
            .equalsCreditorAccountId(creditorAccountId);
        Specification<CreditorTransactionEntity> creditorTransSpec = CreditorTransactionSpecs
            .equalsCreditorAccountId(creditorAccountId);

        // Check the number of rows in the DB before we Act.
        List<ImpositionEntity.Lite> impositions = impositionRepository.findAll(impositionSpec);
        log.info(":deleteMinorCreditorAccount: impositions: {}", impositions);
        assertEquals(2, impositions.size());

        List<CreditorTransactionEntity> creditorTrans = creditorTransactionRepository.findAll(creditorTransSpec);
        log.info(":deleteMinorCreditorAccount: creditor transactions: {}", creditorTrans);
        assertEquals(2, creditorTrans.size());

        Optional<PartyEntity> party = partyRepository.findById(partyId);
        log.info(":deleteMinorCreditorAccount: party: {}", party);
        assertTrue(party.isPresent());

        Optional<CreditorAccountEntity.Lite> creditAccount = creditorAccountRepository.findById(creditorAccountId);
        log.info(":deleteMinorCreditorAccount: creditAccount: {}", creditAccount);
        assertTrue(creditAccount.isPresent());

        // Act
        ResultActions actions = mockMvc.perform(delete(URL_BASE + "/" + creditorAccountId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("If-Match", "0")
                            .param("ignore_missing", "false"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":deleteMinorCreditorAccount: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.message")
                           .value("Creditor Account '" + creditorAccountId + "' deleted"));

        // Assert
        assertEquals(0, impositionRepository.findAll(impositionSpec).size());
        assertEquals(0, creditorTransactionRepository.findAll(creditorTransSpec).size());
        assertFalse(partyRepository.findById(partyId).isPresent());
        assertFalse(creditorAccountRepository.findById(creditorAccountId).isPresent());

    }

}
