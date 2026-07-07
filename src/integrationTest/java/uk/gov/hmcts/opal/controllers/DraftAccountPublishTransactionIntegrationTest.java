package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.containsString;
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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.DraftAccountPublishTransactionIntegrationTest")
@DisplayName("DraftAccountPublishTransactionIntegrationTest")
class DraftAccountPublishTransactionIntegrationTest extends AbstractIntegrationTest {

    private static final long DRAFT_ACCOUNT_ID = 9_999_901L;
    private static final String DRAFT_ACCOUNTS_URL_BASE = "/draft-accounts";
    private static final String DEFENDANT_ACCOUNTS_SEARCH_URL = "/defendant-accounts/search";

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
              "validated_by": "ignored-by-service",
              "validated_by_name": "ignored-by-service",
              "business_unit_id": 77,
              "reason_text": "Approve for publish",
              "version": %d
            }
            """.formatted(version);
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
