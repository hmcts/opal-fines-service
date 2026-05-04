package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
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
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
@Sql(scripts = "classpath:db/insertData/insert_into_minor_creditors.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_minor_creditors.sql", executionPhase = AFTER_TEST_METHOD)
@Slf4j(topic = "opal.OpalMinorCreditorIntegrationTest")
public class OpalMinorCreditorIntegrationTest extends MinorCreditorControllerIntegrationTest {

    @Autowired
    private ImpositionRepository impositionRepository;

    @Autowired
    private CreditorTransactionRepository creditorTransactionRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private CreditorAccountRepository creditorAccountRepository;

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6210")
    void testPostSearchMinorCreditor_Success() throws Exception {
        super.postSearchMinorCreditorImpl_Success(log);
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6195")
    void search_checkLetterReturnsBoth() throws Exception {
        super.search_checkLetter_returnsBoth(log);
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6221")
    void search_NoCheckLetterReturnsBoth() throws Exception {
        super.search_noCheckLetter_returnsBoth(log);
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6220")
    void search_noResultsForUnknownBusinessUnit_returnsEmpty() throws Exception {
        super.search_noResultsForUnknownBusinessUnit_returnsEmpty(log);
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6213")
    void search_orgNamePrefix_normalizedMatches() throws Exception {
        super.search_orgNamePrefix_normalizedMatches(log);
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6208")
    void search_accountNumber_withWildcardChars_treatedLiterally() throws Exception {
        super.search_accountNumber_withWildcardChars_treatedLiterally(log);
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6182")
    void postSearch_missingAuthHeader_returns401ProblemJson() throws Exception {
        super.postSearch_missingAuthHeader_returns401();
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6188")
    void postSearch_invalidToken_returns401ProblemJson() throws Exception {
        super.postSearch_invalidToken_returns401ProblemJson();
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6174")
    void postSearch_authenticatedWithoutPermission_returns403ProblemJson() throws Exception {
        super.postSearch_authenticatedWithoutPermission_returns403ProblemJson();
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6177")
    void testAC1b_ActiveAccountsOnlyTrue() throws Exception {
        super.testAC1b_ActiveAccountsOnlyTrue(log);
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6199")
    void testAC1b_ActiveAccountsOnlyFalse() throws Exception {
        super.testAC1b_ActiveAccountsOnlyFalse(log);
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6217")
    void testAC1a_MultiParam_ForenamesAndSurname() throws Exception {
        super.testAC1a_MultiParam_ForenamesAndSurname(log);
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6184")
    void testAC1a_MultiParam_PostcodeAndBusinessUnitAndAccountNumber() throws Exception {
        super.testAC1a_MultiParam_PostcodeAndAccountNumber(log);
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6185")
    void testAC1a_MultiParam_OrganisationAndAddress() throws Exception {
        super.testAC1a_MultiParam_OrganisationAndAddress(log);
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6181")
    void testAC1ai_BusinessUnitFiltering() throws Exception {
        super.testAC1ai_BusinessUnitFiltering(log);
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6216")
    void testAC2a_ExactMatchSurnameEnabled() throws Exception {
        super.testAC2a_ExactMatchSurnameEnabled(log);
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6196")
    void testAC2ai_ExactMatchSurnameDisabled() throws Exception {
        super.testAC2ai_ExactMatchSurnameDisabled(log);
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6214")
    void testAC2b_ExactMatchForenamesEnabled() throws Exception {
        super.testAC2b_ExactMatchForenamesEnabled(log);
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6186")
    void testAC2bi_ExactMatchForenamesDisabled() throws Exception {
        super.testAC2bi_ExactMatchForenamesDisabled(log);
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6198")
    void testAC2c_AddressLine1StartsWith() throws Exception {
        super.testAC2c_AddressLine1StartsWith(log);
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6183")
    void testAC2c_PostcodeStartsWith() throws Exception {
        super.testAC2c_PostcodeStartsWith(log);
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6223")
    void testAC3a_CompanyNameExactMatch() throws Exception {
        super.testAC3a_CompanyNameExactMatch(log);
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6211")
    void testAC3ai_CompanyNameStartsWith() throws Exception {
        super.testAC3ai_CompanyNameStartsWith(log);
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6189")
    void testAC3ai_CompanyNameStartsWithPartial() throws Exception {
        super.testAC3ai_CompanyNameStartsWithPartial(log);
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6207")
    void testAC3b_CompanyAddressLine1StartsWith() throws Exception {
        super.testAC3b_CompanyAddressLine1StartsWith(log);
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6222")
    void testAC3b_CompanyPostcodeStartsWith() throws Exception {
        super.testAC3b_CompanyPostcodeStartsWith(log);
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6219")
    void testAC3b_CompanyAddressAndPostcodeCombined() throws Exception {
        super.testAC3b_CompanyAddressAndPostcodeCombined(log);
    }

    @Test
    @JiraStory("PO-1911")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6209")
    void getHeaderSummary_success() throws Exception {
        super.getHeaderSummaryImpl_Success(log);
    }

    @Test
    @JiraStory("PO-1911")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6192")
    void getHeaderSummary_notFound_returns404() throws Exception {
        super.getHeaderSummary_notFound_returns404(log);
    }

    @Test
    @JiraStory("PO-1915")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6200")
    void patchMinorCreditor_payoutHold_success() throws Exception {
        super.patchMinorCreditor_payoutHold_success(log);
    }

    @Test
    @JiraStory("PO-1915")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6180")
    void patchMinorCreditor_success_createsAmendments() throws Exception {
        super.patchMinorCreditor_success_createsAmendments(log);
    }

    @Test
    @JiraStory("PO-1915")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6191")
    void patchMinorCreditor_withoutPermission_returns403() throws Exception {
        super.patchMinorCreditor_withoutPermission_returns403();
    }

    @Test
    @JiraStory("PO-1915")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6187")
    void patchMinorCreditor_withoutHoldPermission_returns403() throws Exception {
        super.patchMinorCreditor_withoutHoldPermission_returns403();
    }

    @Test
    @JiraStory("PO-1915")
    @JiraEpic("PO-812")
    void patchMinorCreditor_withoutHoldPermission_holdUnchanged_returns200() throws Exception {
        super.patchMinorCreditor_withoutHoldPermission_holdUnchanged_returns200(log);
    }

    @Test
    @JiraStory("PO-1915")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6179")
    void patchMinorCreditor_withoutAccountMaintenancePermission_returns403() throws Exception {
        super.patchMinorCreditor_withoutAccountMaintenancePermission_returns403();
    }

    @Test
    @JiraStory("PO-1915")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6176")
    void patchMinorCreditor_notFound_returns404() throws Exception {
        super.patchMinorCreditor_notFound_returns404();
    }

    @Test
    @JiraStory("PO-1915")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6218")
    void patchMinorCreditor_staleVersion_returns409() throws Exception {
        super.patchMinorCreditor_staleVersion_returns409();
    }

    @Test
    @JiraStory("PO-1915")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6212")
    void patchMinorCreditor_missingAuthHeader_returns401() throws Exception {
        super.patchMinorCreditor_missingAuthHeader_returns401();
    }

    @Test
    @JiraStory("PO-1915")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6215")
    void patchMinorCreditor_timeout_returns408() throws Exception {
        super.patchMinorCreditor_timeout_returns408(log);
    }

    @Test
    @JiraStory("PO-1915")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6224")
    void patchMinorCreditor_serviceUnavailable_returns503() throws Exception {
        super.patchMinorCreditor_serviceUnavailable_returns503(log);
    }

    @Test
    @JiraStory("PO-1915")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6203")
    void patchMinorCreditor_serverError_returns500() throws Exception {
        super.patchMinorCreditor_serverError_returns500(log);
    }

    @Test
    @JiraStory("PO-1915")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6206")
    void patchMinorCreditor_missingPayload_returns400() throws Exception {
        super.patchMinorCreditor_missingPayload_returns400();
    }

    @Test
    @JiraStory("PO-1911")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6190")
    void getHeaderSummary_missingAuthHeader_returns401() throws Exception {
        super.getHeaderSummary_missingAuthHeader_returns401();
    }

    @Test
    @JiraStory("PO-1911")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6201")
    void getHeaderSummary_authenticatedWithoutPermission_returns403() throws Exception {
        super.getHeaderSummary_authenticatedWithoutPermission_returns403();
    }

    @Test
    @JiraStory("PO-1911")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6194")
    void getHeaderSummary_timeout_returns408() throws Exception {
        super.getHeaderSummary_timeout_returns408(log);
    }

    @Test
    @JiraStory("PO-1911")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6204")
    void getHeaderSummary_serviceUnavailable_returns503() throws Exception {
        super.getHeaderSummary_serviceUnavailable_returns503(log);
    }

    @Test
    @JiraStory("PO-1911")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6178")
    void getHeaderSummary_serverError_returns500() throws Exception {
        super.getHeaderSummary_serverError_returns500(log);
    }

    @Test
    @JiraStory("PO-1914")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6175")
    void getAtAGlance_Success() throws Exception {
        super.getMinorCreditorAtAGlanceImpl_Success(log);
    }

    @Test
    @JiraStory("PO-1914")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6205")
    void getAtAGlance_creditorNotFound() throws Exception {
        super.getMinorCreditorAtAGlanceImpl_failure_creditorNotFound(log);
    }

    @Test
    @JiraStory("PO-1914")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6202")
    void getAtAGlance_serverError_returns500() throws Exception {
        super.getMinorCreditorAtAGlanceImpl_serverError_throws500(log);
    }

    @Test
    @JiraStory("PO-1910")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6193")
    void deleteMinorCreditorAccount() throws Exception {
        // Arrange
        final Long creditorAccountId = 606L;
        final Long partyId = 99007L;
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        Specification<ImpositionEntity> impositionSpec = ImpositionSpecs
            .equalsCreditorAccountId(creditorAccountId);
        Specification<CreditorTransactionEntity> creditorTransSpec = CreditorTransactionSpecs
            .equalsCreditorAccountId(creditorAccountId);

        // Check the number of rows in the DB before we Act.
        List<ImpositionEntity> impositions = impositionRepository.findAll(impositionSpec);
        log.info(":deleteMinorCreditorAccount: impositions: {}", impositions);
        assertEquals(2, impositions.size());

        List<CreditorTransactionEntity> creditorTrans = creditorTransactionRepository.findAll(creditorTransSpec);
        log.info(":deleteMinorCreditorAccount: creditor transactions: {}", creditorTrans);
        assertEquals(2, creditorTrans.size());

        Optional<PartyEntity> party = partyRepository.findById(partyId);
        log.info(":deleteMinorCreditorAccount: party: {}", party);
        assertTrue(party.isPresent());

        Optional<CreditorAccountEntity> creditAccount = creditorAccountRepository.findById(creditorAccountId);
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
