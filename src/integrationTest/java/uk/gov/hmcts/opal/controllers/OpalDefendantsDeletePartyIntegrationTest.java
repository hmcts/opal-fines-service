package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@Slf4j(topic = "opal.OpalDefendantsDeletePartyIntegrationTest")
class OpalDefendantsDeletePartyIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    @Test
    @DisplayName("OPAL: DELETE Remove DAP - Happy path (removed association + bumps version")
    @JiraStory("PO-1897")
    @JiraEpic("PO-1970")
    @JiraTestKey("PO-6017")
    void delete_happyPath_removesAssociation_returnsResponse() throws Exception {
        authoriseAllPermissions();

        long defendantAccountId = 2006L;
        long dapId = 2006L;

        Integer currentVersion = versionFor(defendantAccountId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

        String body = """
            {
              "party_details": {
                "party_id": "206"
              }
            }
            """;

        Integer associationCountBefore = getAssociationCountForDAP(defendantAccountId, dapId);

        ResultActions res = mockMvc.perform(
            delete("/defendant-accounts/2006/defendant-account-parties/2006")
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
        Integer associationCountAfter = getAssociationCountForDAP(defendantAccountId, dapId);
        assertEquals(0, associationCountAfter);

        Integer updatedVersion = versionFor(defendantAccountId);
        assertEquals(currentVersion + 1, updatedVersion);
    }

    private @Nullable Integer getAssociationCountForDAP(Long defAccountId, Long dapId) {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM defendant_account_parties "
                 + "WHERE defendant_account_id = ? AND defendant_account_party_id = ?",
            Integer.class,
            defAccountId,
            dapId
        );
    }

    @Test
    @DisplayName("OPAL: DELETE Remove DAP – Not Found (DAP not on account)")
    @JiraStory("PO-1897")
    @JiraEpic("PO-1970")
    @JiraTestKey("PO-6019")
    void delete_notFound_whenDefendantAccountPartyNotOnAccount() throws Exception {
        authoriseAllPermissions();

        Integer currentVersion = versionFor(78L);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
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
        authoriseAllPermissions();

        Integer currentVersion = versionFor(2006L);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "99");
        headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

        String body = """
            {
              "defendant_account_party_id": "2006"
            }
            """;

        ResultActions res = mockMvc.perform(
            delete("/defendant-accounts/2006/defendant-account-parties/2006")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        log.info("DELETE DAP wrong BU response:\n{}", res.andReturn().getResponse().getContentAsString());

        res.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"));
    }
    
}
