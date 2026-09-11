package uk.gov.hmcts.opal.controllers.r1b;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@Slf4j(topic = "opal.DefendantPartyDeleteIntegrationTest")
class DefendantPartyDeleteIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    @Test
    @DisplayName("OPAL: DELETE Remove DAP - account controls return 422 for blocked account status")
    @JiraStory("PO-5757")
    @JiraEpic("PO-2990")
    @JiraTestKey("PO-9385")
    void delete_removeParty_returns422_whenBlockedByAccountControls() throws Exception {
        // Arrange
        long defendantAccountId = 9077L;
        Integer currentVersion = versionFor(defendantAccountId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

        String body = """
            {
              "party_details": {
                "party_id": "77"
              }
            }
            """;

        // Act
        ResultActions res = mockMvc.perform(
            delete("/defendant-accounts/9077/defendant-account-parties/9077")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        log.info("DELETE DAP account controls response:\n{}", res.andReturn().getResponse().getContentAsString());

        // Assert
        res.andExpect(status().isUnprocessableEntity())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Unprocessable Content"))
            .andExpect(jsonPath("$.status").value(422))
            .andExpect(jsonPath("$.detail").value(
                "Defendant account update blocked: Account Status Check failed because account_status is CS."))
            .andExpect(jsonPath("$.retriable").value(false));

        assertEquals(currentVersion, versionFor(defendantAccountId));
        long dapId = 9077L;
        assertEquals(1, partyAssociationCountFor(defendantAccountId, dapId));
    }

    @Test
    @DisplayName("OPAL: DELETE Remove DAP - Happy path (removed association + bumps version")
    @JiraStory("PO-1897")
    @JiraEpic("PO-1970")
    @JiraTestKey("PO-6017")
    void delete_happyPath_removesAssociation_returnsResponse() throws Exception {

        long defendantAccountId = 2006L;
        long dapId = 2006L;

        Integer currentVersion = versionFor(defendantAccountId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

        String body = """
            {
              "party_details": {
                "party_id": "206"
              }
            }
            """;

        int associationCountBefore = partyAssociationCountFor(defendantAccountId, dapId);

        ResultActions res = mockMvc.perform(
            delete("/defendant-accounts/2006/defendant-account-parties/2006")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        log.info("DELETE DAP happy path response:\n{}", res.andReturn().getResponse().getContentAsString());

        res.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string(HttpHeaders.ETAG, "\"" + (currentVersion + 1) + "\""))
            .andExpect(jsonPath("$.defendant_account_party_id").value("2006"));

        // Assert that 1 association existing before the deletion
        assertEquals(1, associationCountBefore);

        // Assert that associated DAP count dropped after deletion
        int associationCountAfter = partyAssociationCountFor(defendantAccountId, dapId);
        assertEquals(0, associationCountAfter);

        Integer updatedVersion = versionFor(defendantAccountId);
        assertEquals(currentVersion + 1, updatedVersion);
    }

    @Test
    @DisplayName("OPAL: DELETE Remove DAP - bad request when body has no party reference")
    @JiraStory("PO-8982")
    @JiraEpic("PO-2873")
    void delete_badRequest_whenBodyHasNoPartyReference() throws Exception {
        long defendantAccountId = 2006L;
        Integer currentVersion = versionFor(defendantAccountId);

        ResultActions res = performDelete(defendantAccountId, 2006L, "\"" + currentVersion + "\"", """
            {
              "version": 1
            }
            """);

        res.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400));

        assertEquals(currentVersion, versionFor(defendantAccountId));
    }

    @Test
    @DisplayName("OPAL: DELETE Remove DAP - bad request when defendant account party id is blank")
    @JiraStory("PO-8982")
    @JiraEpic("PO-2873")
    void delete_badRequest_whenDefendantAccountPartyIdIsBlank() throws Exception {
        long defendantAccountId = 2006L;
        Integer currentVersion = versionFor(defendantAccountId);

        ResultActions res = performDelete(defendantAccountId, 2006L, "\"" + currentVersion + "\"", """
            {
              "defendant_account_party_id": ""
            }
            """);

        res.andExpect(status().isBadRequest());

        assertEquals(currentVersion, versionFor(defendantAccountId));
    }

    @Test
    @DisplayName("OPAL: DELETE Remove DAP - bad request when party id is blank")
    @JiraStory("PO-8982")
    @JiraEpic("PO-2873")
    void delete_badRequest_whenPartyIdIsBlank() throws Exception {
        long defendantAccountId = 2006L;
        Integer currentVersion = versionFor(defendantAccountId);

        ResultActions res = performDelete(defendantAccountId, 2006L, "\"" + currentVersion + "\"", """
            {
              "party_details": {
                "party_id": ""
              }
            }
            """);

        res.andExpect(status().isBadRequest());

        assertEquals(currentVersion, versionFor(defendantAccountId));
    }

    @Test
    @DisplayName("OPAL: DELETE Remove DAP - stale If-Match returns 409 conflict")
    @JiraStory("PO-8982")
    @JiraEpic("PO-2873")
    void delete_conflict_whenIfMatchIsStale() throws Exception {
        long defendantAccountId = 2006L;
        long dapId = 2006L;
        Integer currentVersion = versionFor(defendantAccountId);
        int associationCountBefore = partyAssociationCountFor(defendantAccountId, dapId);

        ResultActions res = performDelete(defendantAccountId, dapId, "\"9999999\"", """
            {
              "party_details": {
                "party_id": "206"
              }
            }
            """);

        res.andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Conflict"))
            .andExpect(jsonPath("$.status").value(409));

        assertEquals(currentVersion, versionFor(defendantAccountId));
        assertEquals(associationCountBefore, partyAssociationCountFor(defendantAccountId, dapId));
    }

    @Test
    @DisplayName("OPAL: DELETE Remove DAP – Not Found (DAP not on account)")
    @JiraStory("PO-1897")
    @JiraEpic("PO-1970")
    @JiraTestKey("PO-6019")
    void delete_notFound_whenDefendantAccountPartyNotOnAccount() throws Exception {

        Integer currentVersion = versionFor(78L);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

        String body = """
            {
              "party_details": {
                "party_id": "99999"
              }
            }
            """;

        ResultActions res = mockMvc.perform(
            delete("/defendant-accounts/78/defendant-account-parties/99999")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        log.info("DELETE DAP party not on account response:\n{}", res.andReturn().getResponse().getContentAsString());

        res.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"));
    }

    @Test
    @DisplayName("OPAL: DELETE Remove DAP – Not Found (account not in BU)")
    @JiraStory("PO-1897")
    @JiraEpic("PO-1970")
    @JiraTestKey("PO-6018")
    void delete_notFound_whenAccountNotInHeaderBU() throws Exception {
        userStateStub.addPermissions((short) 99, FinesPermission.values());
        Integer currentVersion = versionFor(2006L);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "99");
        headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

        String body = """
            {
              "party_details": {
                "party_id": "206"
              }
            }
            """;

        ResultActions res = mockMvc.perform(
            delete("/defendant-accounts/2006/defendant-account-parties/2006")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        log.info("DELETE DAP wrong BU response:\n{}", res.andReturn().getResponse().getContentAsString());

        res.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"));
    }

    private ResultActions performDelete(long defendantAccountId, long defendantAccountPartyId, String ifMatch,
                                        String body) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, ifMatch);

        return mockMvc.perform(
            delete("/defendant-accounts/{defendantAccountId}/defendant-account-parties/{defendantAccountPartyId}",
                   defendantAccountId, defendantAccountPartyId)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );
    }

}
