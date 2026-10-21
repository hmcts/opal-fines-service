package uk.gov.hmcts.opal.controllers.r1b;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.OptimisticLockException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.CreditorTransactionEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.CreditorAccountPaymentDetailsCommon;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommon;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
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
    "launchdarkly.default-flag-values.release-1b=true"
})
@Sql(scripts = "classpath:db/insertData/insert_into_minor_creditors.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_minor_creditors.sql", executionPhase = AFTER_TEST_METHOD)
@Tag("ExtendedTest")
@Slf4j(topic = "opal.OpalMinorCreditorIntegrationTest")
public class OpalMinorCreditorIntegrationTest extends MinorCreditorControllerIntegrationTest {

    private static final long INDIVIDUAL_MINOR_CREDITOR_ACCOUNT_ID = 607L;
    private static final long COMPANY_MINOR_CREDITOR_ACCOUNT_ID = 608L;
    private static final short MINOR_CREDITOR_BUSINESS_UNIT_ID = 10;

    @MockitoBean
    private LoggingService loggingService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Nested
    class PdpoLogging {

        @BeforeEach
        void grantUpdatePermission() {
            userStateStub.setupWithNoPermissions();
            userStateStub.addPermissions((short) 10, FinesPermission.ACCOUNT_MAINTENANCE_MINOR_CREDITOR);
        }

        @Test
        @JiraStory("PO-10765")
        @JiraEpic("PO-10729")
        void updateIndividual_queuesAlterationAndPreservesResponseAndVersionBehaviour() throws Exception {
            int version = currentVersion();
            int amendmentsBefore = amendments().size();

            update(request(false), version)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string(HttpHeaders.ETAG, "\"" + (version + 1) + "\""))
                .andExpect(jsonPath("$.creditor_account_id").value(607))
                .andExpect(jsonPath("$.party_details.individual_details.surname").value("Updated"))
                .andExpect(jsonPath("$.address.postcode").value("NW1 1AA"))
                .andExpect(jsonPath("$.payment.account_name").value("Creditor Updated"))
                .andExpect(jsonPath("$.payment.hold_payment").value(true));

            assertEquals(version + 2, currentVersion());
            assertTrue(amendments().size() > amendmentsBefore);
            ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
                ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
            verify(loggingService).personalDataAccessLogAsync(captor.capture());
            PersonalDataProcessingLogDetails details = captor.getValue();
            assertEquals("Update Minor Creditor", details.getBusinessIdentifier());
            assertEquals(PersonalDataProcessingCategory.ALTERATION, details.getCategory());
            assertEquals("500000000", details.getCreatedBy().getIdentifier());
            assertEquals(PdplIdentifierType.OPAL_USER_ID, details.getCreatedBy().getType());
            assertEquals(1, details.getIndividuals().size());
            assertEquals("607", details.getIndividuals().getFirst().getIdentifier());
            assertEquals(PdplIdentifierType.MINOR_CREDITOR, details.getIndividuals().getFirst().getType());
            assertNotNull(details.getCreatedAt());
        }

        @ParameterizedTest
        @ValueSource(booleans = {false, true})
        @JiraStory("PO-10765")
        @JiraEpic("PO-10729")
        void updateCompany_succeedsWithoutQueueingEvenWhenQueueIsUnavailable(boolean raisesError) throws Exception {
            jdbcTemplate.update("UPDATE parties SET organisation = 'Y', organisation_name = 'Company Ltd' "
                + "WHERE party_id = 99008");
            queueFailure(raisesError);
            int version = currentVersion();

            update(request(true), version)
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ETAG, "\"" + (version + 1) + "\""))
                .andExpect(jsonPath("$.party_details.organisation_flag").value(true))
                .andExpect(jsonPath("$.payment.hold_payment").value(true));

            assertEquals(version + 2, currentVersion());
            verify(loggingService, never()).personalDataAccessLogAsync(any());
        }

        @ParameterizedTest
        @ValueSource(booleans = {false, true})
        @JiraStory("PO-10765")
        @JiraEpic("PO-10729")
        void updateIndividual_whenQueueFails_returns503AndRollsBackAllChanges(boolean raisesError) throws Exception {
            UpdateSnapshot before = snapshot();
            int version = currentVersion();
            queueFailure(raisesError);

            update(request(false), version)
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/pdpl-logging-unavailable"))
                .andExpect(jsonPath("$.detail").value("The personal data logging queue is currently unavailable"))
                .andExpect(jsonPath("$.retriable").value(true))
                .andExpect(jsonPath("$.creditor_account_id").doesNotExist());

            assertEquals(before, snapshot(), "Party, payment, amendments and version must all be rolled back");
            verify(loggingService).personalDataAccessLogAsync(any());
        }

        @ParameterizedTest
        @CsvSource({
            "Validation, 400",
            "Permission, 403",
            "Not found, 404",
            "Stale version, 409",
            "Missing version, 409",
            "Party mismatch, 400"
        })
        @JiraStory("PO-10765")
        @JiraEpic("PO-10729")
        void updateRejected_doesNotQueueSuccessOrChangeData(String scenario, int statusCode) throws Exception {
            UpdateSnapshot before = snapshot();
            PatchMinorCreditorAccountRequest request = request(false);
            Integer version = currentVersion();
            long accountId = 607L;
            switch (scenario) {
                case "Validation" -> request.setPayment(null);
                case "Permission" -> userStateStub.setupWithNoPermissions();
                case "Not found" -> accountId = 999999L;
                case "Stale version" -> version++;
                case "Missing version" -> version = null;
                case "Party mismatch" -> request.getPartyDetails().setPartyId("99007");
                default -> throw new IllegalArgumentException("Unknown scenario: " + scenario);
            }

            update(accountId, request, version).andExpect(status().is(statusCode));

            assertEquals(before, snapshot());
            verify(loggingService, never()).personalDataAccessLogAsync(any());
        }

        @Test
        @JiraStory("PO-10765")
        @JiraEpic("PO-10729")
        void concurrentUpdateDuringPersistence_doesNotQueueSuccessAndRollsBackLosingUpdate() throws Exception {
            final UpdateSnapshot before = snapshot();
            int version = currentVersion();
            TransactionTemplate concurrentTransaction = new TransactionTemplate(transactionManager);
            concurrentTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            doAnswer(invocation -> {
                concurrentTransaction.executeWithoutResult(transaction -> assertEquals(1, jdbcTemplate.update(
                    "UPDATE creditor_accounts SET version_number = version_number + 1 "
                        + "WHERE creditor_account_id = 607")));
                // The account is already managed; the service's flush performs the actual write.
                return invocation.getArgument(0);
            }).when(creditorAccountRepository).save(any(CreditorAccountEntity.class));

            // Preserve the existing persistence-exception response; stale If-Match is covered separately.
            var result = update(request(false), version)
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/servlet-error"))
                .andReturn();
            assertInstanceOf(OptimisticLockException.class, result.getResolvedException());

            assertEquals(version + 1, currentVersion(), "Only the concurrent transaction's version must persist");
            UpdateSnapshot after = snapshot();
            assertEquals(before.party(), after.party());
            assertEquals(before.amendments(), after.amendments());
            Map<String, Object> expectedAccount = new HashMap<>(before.account());
            expectedAccount.put("version_number", after.account().get("version_number"));
            assertEquals(expectedAccount, after.account(), "The losing update must not change payment details");
            verify(loggingService, never()).personalDataAccessLogAsync(any());
        }

        private void queueFailure(boolean raisesError) {
            UpdateSnapshot before = snapshot();
            when(loggingService.personalDataAccessLogAsync(any())).thenAnswer(invocation -> {
                assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
                UpdateSnapshot pending = snapshot();
                assertNotEquals(before.party(), pending.party(), "Party changes must be flushed before queueing");
                assertNotEquals(before.account(), pending.account(), "Account changes must be flushed before queueing");
                assertTrue(pending.amendments().size() > before.amendments().size(),
                    "Amendments must be written before queueing");
                if (raisesError) {
                    throw new IllegalStateException("Queue unavailable");
                }
                return false;
            });
        }

        private int currentVersion() {
            return jdbcTemplate.queryForObject(
                "SELECT version_number FROM creditor_accounts WHERE creditor_account_id = 607", Integer.class);
        }

        private List<Map<String, Object>> amendments() {
            return jdbcTemplate.queryForList("SELECT * FROM amendments "
                + "WHERE associated_record_type = 'creditor_accounts' "
                + "AND associated_record_id = '607' ORDER BY amendment_id");
        }

        private UpdateSnapshot snapshot() {
            return new UpdateSnapshot(
                jdbcTemplate.queryForMap("SELECT * FROM parties WHERE party_id = 99008"),
                jdbcTemplate.queryForMap("SELECT * FROM creditor_accounts WHERE creditor_account_id = 607"),
                amendments());
        }

        private ResultActions update(PatchMinorCreditorAccountRequest request, Integer version) throws Exception {
            return update(607L, request, version);
        }

        private ResultActions update(long accountId, PatchMinorCreditorAccountRequest request, Integer version)
            throws Exception {
            var builder = patch(URL_BASE + "/" + accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("Business-Unit-Id", "10")
                .content(objectMapper.writeValueAsString(request));
            if (version != null) {
                builder.header(HttpHeaders.IF_MATCH, "\"" + version + "\"");
            }
            return mockMvc.perform(builder);
        }

        private PatchMinorCreditorAccountRequest request(boolean company) {
            PartyDetailsCommon party = new PartyDetailsCommon().partyId("99008").organisationFlag(company);
            if (company) {
                party.organisationDetails(new OrganisationDetailsCommon().organisationName("Updated Company Ltd"));
            } else {
                party.individualDetails(new IndividualDetailsCommon().surname("Updated").forenames("Creditor"));
            }
            return new PatchMinorCreditorAccountRequest()
                .partyDetails(party)
                .address(new AddressDetailsCommon().addressLine1("99 Updated Road").postcode("NW1 1AA"))
                .payment(new CreditorAccountPaymentDetailsCommon()
                    .accountName("Creditor Updated").sortCode("112233").accountNumber("87654321")
                    .accountReference("MC-REF-01").payByBacs(false).holdPayment(true));
        }
    }

    private record UpdateSnapshot(Map<String, Object> party, Map<String, Object> account,
                                  List<Map<String, Object>> amendments) {
    }

    @MockitoSpyBean
    private ImpositionRepository impositionRepository;

    @MockitoSpyBean
    private CreditorTransactionRepository creditorTransactionRepository;

    @MockitoSpyBean
    private PartyRepository partyRepository;

    @MockitoSpyBean
    private CreditorAccountRepository creditorAccountRepository;

    @BeforeEach
    void configurePdplQueue() {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);
    }

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
    void postSearch_missingAuthHeader_returns403ProblemJson() throws Exception {
        super.postSearch_missingAuthHeader_returns403();
    }

    @Test
    @JiraStory("PO-713")
    @JiraEpic("PO-704")
    @JiraTestKey("PO-6188")
    void postSearch_invalidToken_returns403ProblemJson() throws Exception {
        super.postSearch_invalidToken_returns403ProblemJson();
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
    void testAC1a_MultiParam_PostcodeAndBusinessUnit() throws Exception {
        super.testAC1a_MultiParam_PostcodeAndBusinessUnit(log);
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
    @JiraEpic("PO-10729")
    @JiraStory("PO-10763")
    void getHeaderSummary_LogsViewMinorCreditorAccountForIndividual() throws Exception {
        super.getHeaderSummaryImpl_logsViewMinorCreditorAccount(log);
    }

    @Test
    @JiraEpic("PO-10729")
    @JiraStory("PO-10763")
    void getHeaderSummary_pdplQueueFailureReturns503WithoutPersonalData() throws Exception {
        super.getHeaderSummaryImpl_pdplQueueFailureReturns503WithoutPersonalData(log);
    }

    @Test
    @JiraEpic("PO-10729")
    @JiraStory("PO-10763")
    void getHeaderSummary_doesNotLogViewMinorCreditorAccountForOrganisations() throws Exception {
        super.getHeaderSummaryImpl_DoesNotLogViewMinorCreditorAccountForOrganisation(log);
    }

    @Test
    @JiraStory("PO-1911")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6192")
    void getHeaderSummary_notFound_returns404() throws Exception {
        super.getHeaderSummary_notFound_returns404(log);
    }

    @Test
    @JiraEpic("PO-2630")
    @JiraStory("PO-10472")
    void getHeaderSummary_repayment_returnsRepaymentTrue() throws Exception {
        super.getHeaderSummary_repaymentTrue(log);
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
    @JiraTestKey("PO-6191")
    void patchMinorCreditor_withoutPermission_returns403() throws Exception {
        super.patchMinorCreditor_withoutPermission_returns403();
    }

    @Test
    @JiraStory("PO-1915")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6187")
    void patchMinorCreditor_withMinorCreditorMaintenancePermission_returns200() throws Exception {
        super.patchMinorCreditor_withMinorCreditorMaintenancePermission_returns200();
    }

    @Test
    @JiraStory("PO-1915")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6179")
    void patchMinorCreditor_withoutMinorCreditorMaintenancePermission_returns403() throws Exception {
        super.patchMinorCreditor_withoutMinorCreditorMaintenancePermission_returns403();
    }

    @Test
    @JiraStory("PO-1915")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6212")
    void patchMinorCreditor_missingAuthHeader_returns403() throws Exception {
        super.patchMinorCreditor_missingAuthHeader_returns403();
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
    void getHeaderSummary_missingAuthHeader_returns403() throws Exception {
        super.getHeaderSummary_missingAuthHeader_returns403();
    }

    @Test
    @JiraStory("PO-1911")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6201")
    void getHeaderSummary_authenticatedWithoutPermission_returns403() throws Exception {
        super.getHeaderSummary_authenticatedWithoutPermission_returns403();
    }

    @Test
    @JiraStory("PO-1986")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-7656")
    void getMinorCreditorAccount_success_withBacsPermission_returnsBacsFields() throws Exception {
        super.getMinorCreditorAccount_success_withBacsPermission_returnsBacsFields(log);
    }

    @Test
    @JiraStory("PO-1986")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-7655")
    void getMinorCreditorAccount_success_withoutBacsPermission_redactsBacsFields() throws Exception {
        super.getMinorCreditorAccount_success_withoutBacsPermission_redactsBacsFields(log);
    }

    @Test
    @JiraStory("PO-9786")
    @JiraEpic("PO-2630")
    @JiraTestKey("PO-9786")
    void getMinorCreditorAccount_repaymentAccount_returnsRepaymentTrue() throws Exception {
        super.getMinorCreditorAccountImpl_repaymentTrue(log);
    }

    @Test
    @JiraStory("PO-1986")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-7657")
    void getMinorCreditorAccount_withBacsPermissionInDifferentBusinessUnit_redactsBacsFields() throws Exception {
        super.getMinorCreditorAccount_withBacsPermissionInDifferentBusinessUnit_redactsBacsFields(log);
    }

    @Test
    @JiraStory("PO-1986")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-7653")
    void getMinorCreditorAccount_missingAuthHeader_returns403() throws Exception {
        super.getMinorCreditorAccount_missingAuthHeader_returns403();
    }

    @Test
    @JiraStory("PO-1986")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-7658")
    void getMinorCreditorAccount_authenticatedWithoutPermission_returns403() throws Exception {
        super.getMinorCreditorAccount_authenticatedWithoutPermission_returns403();

        verify(loggingService, never()).personalDataAccessLogAsync(any());
    }

    @Test
    @JiraStory("PO-1986")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-7652")
    void getMinorCreditorAccount_notFound_returns404() throws Exception {
        super.getMinorCreditorAccount_notFound_returns404(log);

        verify(loggingService, never()).personalDataAccessLogAsync(any());
    }

    @Test
    @DisplayName("PDPO: individual minor creditor read queues a consultation log")
    @JiraStory("PO-10761")
    @JiraEpic("PO-10729")
    void getMinorCreditorAccount_individual_queuesConsultationLog() throws Exception {
        authoriseMinorCreditorRead();

        mockMvc.perform(get(URL_BASE + "/{id}", INDIVIDUAL_MINOR_CREDITOR_ACCOUNT_ID)
                .accept(MediaType.APPLICATION_JSON)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(false));

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService).personalDataAccessLogAsync(captor.capture());
        PersonalDataProcessingLogDetails details = captor.getValue();
        List<ParticipantIdentifier> individuals = details.getIndividuals();

        assertAll(
            () -> assertEquals("View Minor Creditor", details.getBusinessIdentifier()),
            () -> assertEquals(PersonalDataProcessingCategory.CONSULTATION, details.getCategory()),
            () -> assertEquals("500000000", details.getCreatedBy().getIdentifier()),
            () -> assertEquals(PdplIdentifierType.OPAL_USER_ID, details.getCreatedBy().getType()),
            () -> assertEquals(List.of(String.valueOf(INDIVIDUAL_MINOR_CREDITOR_ACCOUNT_ID)),
                individuals.stream().map(ParticipantIdentifier::getIdentifier).toList()),
            () -> assertEquals(List.of(PdplIdentifierType.MINOR_CREDITOR),
                individuals.stream().map(ParticipantIdentifier::getType).toList()),
            () -> assertNull(details.getRecipient())
        );
    }

    @Test
    @DisplayName("PDPO: queue rejection prevents individual minor creditor data from being returned")
    @JiraStory("PO-10761")
    @JiraEpic("PO-10729")
    void getMinorCreditorAccount_queueReturnsFalse_returnsServiceUnavailable() throws Exception {
        authoriseMinorCreditorRead();
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(false);

        mockMvc.perform(get(URL_BASE + "/{id}", INDIVIDUAL_MINOR_CREDITOR_ACCOUNT_ID)
                .accept(MediaType.APPLICATION_JSON)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken()))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/pdpl-logging-unavailable"))
            .andExpect(jsonPath("$.detail").value("The personal data logging queue is currently unavailable"))
            .andExpect(jsonPath("$.retriable").value(true))
            .andExpect(jsonPath("$.party_details").doesNotExist());
    }

    @Test
    @DisplayName("PDPO: queue error prevents individual minor creditor data from being returned")
    @JiraStory("PO-10761")
    @JiraEpic("PO-10729")
    void getMinorCreditorAccount_queueRaisesError_returnsServiceUnavailable() throws Exception {
        authoriseMinorCreditorRead();
        when(loggingService.personalDataAccessLogAsync(any()))
            .thenThrow(new RuntimeException("logging unavailable"));

        mockMvc.perform(get(URL_BASE + "/{id}", INDIVIDUAL_MINOR_CREDITOR_ACCOUNT_ID)
                .accept(MediaType.APPLICATION_JSON)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken()))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/pdpl-logging-unavailable"))
            .andExpect(jsonPath("$.party_details").doesNotExist());
    }

    @Test
    @DisplayName("PDPO: company minor creditor read does not queue an individual log")
    @JiraStory("PO-10761")
    @JiraEpic("PO-10729")
    void getMinorCreditorAccount_company_doesNotQueueLog() throws Exception {
        authoriseMinorCreditorRead();

        mockMvc.perform(get(URL_BASE + "/{id}", COMPANY_MINOR_CREDITOR_ACCOUNT_ID)
                .accept(MediaType.APPLICATION_JSON)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(true));

        verify(loggingService, never()).personalDataAccessLogAsync(any());
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
    @JiraTestKey("PO-7654")
    void getAtAGlance_withoutViewCreditorBacsPermission_returns403() throws Exception {
        super.getMinorCreditorAtAGlanceImpl_withoutViewCreditorBacsPermission_returns403();
    }

    @Test
    @JiraStory("PO-1910")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6193")
    void deleteMinorCreditorAccount() throws Exception {
        // Arrange
        final Long creditorAccountId = 606L;
        final Long partyId = 99007L;

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
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
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

    private void authoriseMinorCreditorRead() {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions(MINOR_CREDITOR_BUSINESS_UNIT_ID,
            FinesPermission.SEARCH_AND_VIEW_ACCOUNTS,
            FinesPermission.VIEW_CREDITOR_BACS);
    }

}
