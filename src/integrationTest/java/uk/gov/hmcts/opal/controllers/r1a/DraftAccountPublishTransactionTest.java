package uk.gov.hmcts.opal.controllers.r1a;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.draft.DraftAccountSource;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.entity.draft.DraftAccountType;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.DraftAccountRepository;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaLegacyRepository;
import uk.gov.hmcts.opal.repository.OpalLocalJusticeAreaRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.DraftAccountPublishTransactionTest")
@DisplayName("DraftAccountPublishTransactionIntegrationTest")
@Tag("ExtendedTest")
@Isolated
class DraftAccountPublishTransactionTest extends AbstractIntegrationTest {

    private static final long DRAFT_ACCOUNT_ID = 9_999_901L;
    private static final String DRAFT_ACCOUNTS_URL_BASE = "/draft-accounts";
    private static final String DEFENDANT_ACCOUNTS_SEARCH_URL = "/defendant-accounts/search";

    @MockitoBean
    private LoggingService loggingService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DraftAccountRepository draftAccountRepository;

    @Autowired
    private BusinessUnitRepository businessUnitRepository;

    @Autowired
    private OpalLocalJusticeAreaRepository opalLocalJusticeAreaRepository;

    @Autowired
    private LocalJusticeAreaLegacyRepository localJusticeAreaLegacyRepository;

    @BeforeEach
    void setUpLogging() {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);
    }

    @Test
    @Sql(scripts = "classpath:db/deleteData/delete_from_draft_account_publish_retry.sql",
        executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/deleteData/delete_from_draft_account_publish_retry.sql",
        executionPhase = AFTER_TEST_METHOD)
    void publishFixedPenalty_usesOpalLjaForOriginatorAndImposedBy() throws Exception {
        Short originalCourtLjaId = jdbcTemplate.queryForObject(
            "SELECT local_justice_area_id FROM courts WHERE court_id = ?", Short.class, 770000000021L);
        short courtLjaId = 409;
        var opalArea = opalLocalJusticeAreaRepository.findById(courtLjaId).orElseThrow();
        var legacyArea = localJusticeAreaLegacyRepository.findById(courtLjaId).orElseThrow();
        String originalOpalName = opalArea.getName();
        assertEquals("Burnley Crown Court", originalOpalName);
        assertEquals(0, jdbcTemplate.queryForObject(
            "SELECT count(*) FROM prosecutors WHERE prosecutor_id = ? AND name = ?",
            Integer.class, courtLjaId, originalOpalName));
        String originalLegacyName = legacyArea.getName();
        var draft = draftAccountRepository.saveAndFlush(DraftAccountEntity.builder()
            .businessUnit(businessUnitRepository.findById((short) 77).orElseThrow())
            .createdDate(LocalDateTime.now())
            .submittedBy("SUBMITTER")
            .submittedByName("opal-publish-it")
            .source(DraftAccountSource.MAC)
            .account("""
            {
                  "account_type": "Fixed Penalty",
                  "defendant_type": "adultOrYouthOnly",
                  "originator_id": 409,
                  "originator_name": "Burnley Crown Court",
                  "originator_type": "FP",
                  "account_sentence_date": "2023-02-15",
                  "enforcement_court_id": 770000000021,
                  "collection_order_made": true,
                  "collection_order_date": "2023-02-22",
                  "payment_card_request": null,
                  "prosecutor_case_reference": "PUBLISH-RETRY-IT",
                  "defendant": {
                    "company_flag": false,
                    "surname": "TEST",
                    "forenames": "Publish",
                    "dob": "1990-01-01",
                    "address_line_1": "1 Test Street",
                    "post_code": "TE1 1ST",
                    "telephone_number_home": "02070000000",
                    "debtor_detail": {
                      "document_language": "EN",
                      "hearing_language": null,
                      "aliases": null
                    }
                  },
                  "offences": [
                    {
                      "offence_id": 33369,
                      "date_of_sentence": "2023-02-15",
                      "imposing_court_id": 770000000021,
                      "impositions": [
                        {
                          "result_id": "FO",
                          "amount_imposed": 100.00,
                          "amount_paid": 0.00,
                          "minor_creditor": null,
                          "major_creditor_id": null
                        }
                      ]
                    }
                  ],
                  "payment_terms": {
                    "payment_terms_type_code": "B",
                    "effective_date": null,
                    "instalment_period": null,
                    "instalment_amount": null,
                    "lump_sum_amount": null,
                    "default_days_in_jail": null,
                    "enforcements": null
                  },
                  "account_notes": null,
                  "fp_ticket_detail": {"notice_number": "TEST123"},
                  "suspended_committal_date": null
                }
                """)
            .accountType(DraftAccountType.FIXED_PENALTY)
            .accountSnapshot("""
                {"account_type":"Fixed Penalty","submitted_by":"SUBMITTER","submitted_by_name":"opal-publish-it"}
                """)
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .accountStatusDate(LocalDateTime.now())
            .timelineData("""
                [{"status":"Created","username":"SUBMITTER","reason_text":null,"status_date":"2026-06-22"}]
                """)
            .build());
        try {
            jdbcTemplate.update("UPDATE courts SET local_justice_area_id = ? WHERE court_id = ?",
                courtLjaId, 770000000021L);
            legacyArea.setName("Legacy imposing LJA");
            localJusticeAreaLegacyRepository.saveAndFlush(legacyArea);

            mockMvc.perform(patch(DRAFT_ACCOUNTS_URL_BASE + "/" + draft.getDraftAccountId())
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .header("authorization", userStateStub.getBearerToken())
                    .header("If-Match", "\"" + draft.getVersion() + "\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateRequestBody(0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_status").value("Published"));

            Long accountId = draftAccountRepository.findById(draft.getDraftAccountId()).orElseThrow().getAccountId();
            assertEquals(originalOpalName, jdbcTemplate.queryForObject(
                "SELECT imposed_by_name FROM defendant_accounts WHERE defendant_account_id = ?",
                String.class, accountId));
        } finally {
            jdbcTemplate.update("UPDATE courts SET local_justice_area_id = ? WHERE court_id = ?",
                originalCourtLjaId, 770000000021L);
            legacyArea.setName(originalLegacyName);
            localJusticeAreaLegacyRepository.saveAndFlush(legacyArea);
            draftAccountRepository.deleteById(draft.getDraftAccountId());
        }
    }

    @JiraStory("PO-7911")
    @JiraEpic("PO-973")
    @Test
    @DraftAccountPublishDbUpdateFailureFixture
    @JiraTestKey("PO-8764")
    void publishDraftAccount_publishesButFailsToUpdateStatus_leavesDraftPublishableForRetry() throws Exception {
        // Arrange
        String etag = getDraftEtag();

        // Act: first publish attempt fails while saving the final published status.
        mockMvc.perform(patch(DRAFT_ACCOUNTS_URL_BASE + "/" + DRAFT_ACCOUNT_ID)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("If-Match", etag)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequestBody(extractVersion(etag))))
            .andExpect(status().is5xxServerError());

        // Assert: the draft was not published.
        MvcResult afterFailure = mockMvc.perform(get(DRAFT_ACCOUNTS_URL_BASE + "/" + DRAFT_ACCOUNT_ID)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("Accept", MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.account_number").doesNotExist())
            .andExpect(jsonPath("$.account_id").doesNotExist())
            .andReturn();

        String retryEtag = afterFailure.getResponse().getHeader("ETag");
        assertDefendantAccountSearchCount(0);

        // Act: retry publish using the current ETag/version.
        mockMvc.perform(patch(DRAFT_ACCOUNTS_URL_BASE + "/" + DRAFT_ACCOUNT_ID)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("If-Match", retryEtag)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequestBody(extractVersion(retryEtag))))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.account_status").value("Published"))
            .andExpect(jsonPath("$.account_number").isNotEmpty())
            .andExpect(jsonPath("$.account_id").isNumber());

        assertDefendantAccountSearchCount(1);
    }

    @JiraStory("PO-7911")
    @JiraEpic("PO-973")
    @Test
    @DraftAccountPublishSpFailureFixture
    @JiraTestKey("PO-8765")
    void publishDraftAccount_storedProcFailure_PersistsPublishingFailedStatusAndTimelineData() throws Exception {
        mockMvc.perform(patch(DRAFT_ACCOUNTS_URL_BASE + "/" + DRAFT_ACCOUNT_ID)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("If-Match", "0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequestBody(0)))
            .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get(DRAFT_ACCOUNTS_URL_BASE + "/" + DRAFT_ACCOUNT_ID)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("Accept", MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"2\""))
            .andExpect(jsonPath("$.account_status").value("Publishing Failed"))
            .andExpect(jsonPath("$.account_number").doesNotExist())
            .andExpect(jsonPath("$.account_id").doesNotExist())
            .andExpect(jsonPath("$.status_message").value("Stored Procedure Failure."))
            .andExpect(jsonPath("$.timeline_data[1].status").value("Publishing Pending"))
            .andExpect(jsonPath("$.timeline_data[2].status").value("Publishing Failed"))
            .andExpect(jsonPath("$.timeline_data[2].reason_text",
                containsString("Stored Procedure Failure.")))
            .andExpect(jsonPath("$.timeline_data[2].reason_text", containsString("Error code:"))
            );

        assertDefendantAccountSearchCount(0);
    }

    private String updateRequestBody(int version) {
        return """
            {
              "account_status": "Publishing Pending",
              "business_unit_id": 77,
              "reason_text": "Approve for publish"
            }
            """;
    }

    private void assertDefendantAccountSearchCount(int expectedCount) throws Exception {
        mockMvc.perform(post(DEFENDANT_ACCOUNTS_SEARCH_URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "active_accounts_only": true,
                      "business_unit_ids": [77],
                      "reference_number": {
                        "account_number": null,
                        "prosecutor_case_reference": "PUBLISH-RETRY-IT",
                        "organisation": false
                      },
                      "defendant": null,
                      "consolidation_search": false
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(expectedCount));
    }

    private String getDraftEtag() throws Exception {
        MvcResult result = mockMvc.perform(get(DRAFT_ACCOUNTS_URL_BASE + "/" + DRAFT_ACCOUNT_ID)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("Accept", MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andReturn();

        return result.getResponse().getHeader("ETag");
    }

    private int extractVersion(String etag) {
        return Integer.parseInt(etag.replace("\"", ""));
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Sql(
        scripts = "classpath:db/deleteData/delete_from_draft_account_publish_retry.sql",
        executionPhase = BEFORE_TEST_METHOD
    )
    @Sql(
        scripts = "classpath:db/insertData/insert_into_draft_account_publish_final_status_failure.sql",
        config = @SqlConfig(separator = "@@"),
        executionPhase = BEFORE_TEST_METHOD
    )
    @Sql(
        scripts = "classpath:db/deleteData/delete_from_draft_account_publish_retry.sql",
        executionPhase = AFTER_TEST_METHOD
    )
    private @interface DraftAccountPublishDbUpdateFailureFixture {

    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Sql(
        scripts = "classpath:db/deleteData/delete_from_draft_account_publish_retry.sql",
        executionPhase = BEFORE_TEST_METHOD
    )
    @Sql(
        scripts = "classpath:db/insertData/insert_into_draft_account_publish_retry.sql",
        config = @SqlConfig(separator = "@@"),
        executionPhase = BEFORE_TEST_METHOD
    )
    @Sql(
        scripts = "classpath:db/insertData/insert_into_draft_account_publish_sp_failure.sql",
        config = @SqlConfig(separator = "@@"),
        executionPhase = BEFORE_TEST_METHOD
    )
    @Sql(
        scripts = "classpath:db/deleteData/delete_from_draft_account_publish_retry.sql",
        executionPhase = AFTER_TEST_METHOD
    )
    public @interface DraftAccountPublishSpFailureFixture {

    }
}
