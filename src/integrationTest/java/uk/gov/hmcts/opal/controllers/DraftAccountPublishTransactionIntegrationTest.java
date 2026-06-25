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
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

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
    @DraftAccountPublishRetryFixture
    void publishDraftAccount_publishesButFailsToUpdateStatus_leavesDraftPublishableForRetry() throws Exception {
        // Arrange: @Sql creates a submitted draft and installs a trigger that fails the final status update.

        // Act: approve the draft through the public PATCH endpoint, which starts the publish flow.
        mockMvc.perform(patch(DRAFT_ACCOUNTS_URL_BASE + "/" + DRAFT_ACCOUNT_ID)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("If-Match", "0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequestBody(0)))
            .andExpect(status().is5xxServerError());

        // Assert: the stored proc created the defendant account, but the draft never saved the returned identifiers.
        mockMvc.perform(get(DRAFT_ACCOUNTS_URL_BASE + "/" + DRAFT_ACCOUNT_ID)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("Accept", MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"2\""))
            .andExpect(jsonPath("$.account_status").value("Publishing Failed"))
            .andExpect(jsonPath("$.account_number").doesNotExist())
            .andExpect(jsonPath("$.account_id").doesNotExist());

        // Assert: atomic rollback means no defendant account was left behind after the failed publish.
        assertDefendantAccountSearchCount(0);

        // Act: retry the same draft through PATCH. Because the draft has no account number, it is publishable again.
        mockMvc.perform(patch(DRAFT_ACCOUNTS_URL_BASE + "/" + DRAFT_ACCOUNT_ID)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("If-Match", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequestBody(1)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"4\""))
            .andExpect(jsonPath("$.account_status").value("Published"))
            .andExpect(jsonPath("$.account_number").isNotEmpty())
            .andExpect(jsonPath("$.account_id").isNumber());

        // Assert: exactly one defendant account exists after the successful retry.
        assertDefendantAccountSearchCount(1);
    }


    @JiraStory("PO-7911")
    @JiraEpic("PO-973")
    @Test
    @DraftAccountPublishSpFailureFixture
    void publishDraftAccount_storedProcFailure_PersistsPublishingFailedStatusAndTimelineData() throws Exception {
        mockMvc.perform(patch(DRAFT_ACCOUNTS_URL_BASE + "/" + DRAFT_ACCOUNT_ID)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("If-Match", "0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequestBody(0)))
            .andExpect(status().is5xxServerError());

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
        scripts = "classpath:db/deleteData/delete_from_draft_account_publish_retry.sql",
        executionPhase = AFTER_TEST_METHOD
    )
    private @interface DraftAccountPublishRetryFixture {

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
