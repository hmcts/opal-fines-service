package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@Slf4j(topic = "opal.OpalDefendantsPaymentCardIntegrationTest")
class OpalDefendantsPaymentCardIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    @Test
    @DisplayName("OPAL: Add Payment Card Request – Happy Path [@PO-1719]")
    @JiraStory("PO-1719")
    @JiraEpic("PO-977")
    @JiraTestKey("PO-6039")
    void opalAddPaymentCardRequest_Happy() throws Exception {
        Integer currentVersion = versionFor(901L);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "78");
        headers.add("Business-Unit-User-Id", "TEST_USER_123");
        headers.add("If-Match", "\"" + currentVersion + "\"");

        ResultActions result = mockMvc.perform(
            post("/defendant-accounts/901/payment-card-request")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        );

        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":opalAddPaymentCardRequest_Happy body:\n{}", ToJsonString.toPrettyJson(body));

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_id").value(901));
    }

    @Test
    @DisplayName("OPAL: Add Payment Card Request – Not Found when account not in header BU [@PO-1719]")
    @JiraStory("PO-1719")
    @JiraEpic("PO-977")
    @JiraTestKey("PO-6041")
    void opalAddPaymentCardRequest_NotFound_WrongBU() throws Exception {
        Integer currentVersion = versionFor(88L);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "99");
        headers.add("If-Match", "\"" + currentVersion + "\"");

        ResultActions result = mockMvc.perform(
            post("/defendant-accounts/88/payment-card-request")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        );

        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":opalAddPaymentCardRequest_NotFound_WrongBU body:\n{}", body);

        result.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"))
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("OPAL: Add Payment Card Request – Forbidden when user lacks permission [@PO-1719]")
    @JiraStory("PO-1719")
    @JiraEpic("PO-977")
    @JiraTestKey("PO-6040")
    void opalAddPaymentCardRequest_Forbidden_NoPermission() throws Exception {
        userStateStub.setupWithNoPermissions();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token_without_permission");
        headers.add("Business-Unit-Id", "78");
        headers.add("If-Match", "\"0\"");

        mockMvc.perform(
                post("/defendant-accounts/88/payment-card-request")
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"));
    }

    @Test
    @DisplayName("OPAL: Add Payment Card Request – Forbiden when missing auth header [@PO-1719]")
    @JiraStory("PO-1719")
    @JiraEpic("PO-977")
    @JiraTestKey("PO-6038")
    void opalAddPaymentCardRequest_Unauthorized() throws Exception {
        userStateStub.setupWithNoPermissions();

        mockMvc.perform(
                post("/defendant-accounts/88/payment-card-request")
                    .header("Business-Unit-Id", "78")
                    .header("If-Match", "\"0\"")
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail").value("You do not have permission to access this resource"))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @Test
    @DisplayName("OPAL: Add Payment Card Request – Conflict when If-Match does not match [@PO-1719]")
    @JiraStory("PO-1719")
    @JiraEpic("PO-977")
    @JiraTestKey("PO-6042")
    void opalAddPaymentCardRequest_IfMatchConflict() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "78");
        headers.add("If-Match", "\"9999\"");

        mockMvc.perform(
                post("/defendant-accounts/88/payment-card-request")
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/optimistic-locking"));
    }

    @Test
    @DisplayName("OPAL: Add Payment Card Request – Conflict when PCR already exists [@PO-1719]")
    @JiraStory("PO-1719")
    @JiraEpic("PO-977")
    @JiraTestKey("PO-6043")
    void opalAddPaymentCardRequest_AlreadyExists() throws Exception {

        Integer version1 = versionFor(88L);
        log.info("INITIAL VERSION = {}", version1);

        HttpHeaders headers1 = new HttpHeaders();
        headers1.add(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken());
        headers1.add("Business-Unit-Id", "78");
        headers1.add("Business-Unit-User-Id", "TEST_USER_123");
        headers1.add("If-Match", "\"" + version1 + "\"");

        mockMvc.perform(
            post("/defendant-accounts/88/payment-card-request")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        ).andExpect(status().isOk());

        Integer version2 = versionFor(88L);

        HttpHeaders headers2 = new HttpHeaders();
        headers2.setBearerAuth("some_value");
        headers2.add("Business-Unit-Id", "78");
        headers2.add("Business-Unit-User-Id", "TEST_USER_123");
        headers2.add("If-Match", "\"" + version2 + "\"");

        ResultActions result = mockMvc.perform(
            post("/defendant-accounts/88/payment-card-request")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers2)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        );

        result.andExpect(status().isConflict())
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/resource-conflict"))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("A payment card request already exists for this account."))
            .andExpect(jsonPath("$.resourceType").value("DefendantAccountEntity"))
            .andExpect(jsonPath("$.resourceId").value("88"))
            .andExpect(jsonPath("$.retriable").value(true))
            .andExpect(jsonPath("$.conflictReason").doesNotExist());
    }
}
