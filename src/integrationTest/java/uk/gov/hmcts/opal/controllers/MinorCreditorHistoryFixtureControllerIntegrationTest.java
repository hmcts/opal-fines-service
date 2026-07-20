package uk.gov.hmcts.opal.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.MinorCreditorHistoryFixtureControllerIntegrationTest")
@DisplayName("MinorCreditorHistoryFixtureController Integration Test")
class MinorCreditorHistoryFixtureControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/testing-support/minor-creditor-history";

    @Test
    @JiraStory("PO-2642")
    @JiraEpic("PO-2653")
    void shouldCreateAndDeleteMinorCreditorHistoryFixture() throws Exception {
        ResultActions createActions = mockMvc.perform(post(URL_BASE)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"reference\":\"MCHINT\"}"));

        MvcResult createResult = createActions.andReturn();
        String body = createResult.getResponse().getContentAsString();
        log.info(":shouldCreateAndDeleteMinorCreditorHistoryFixture: Response body:\n"
                 + ToJsonString.toPrettyJson(body));

        createActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.creditor_account_id").isNumber())
            .andExpect(jsonPath("$.defendant_account_id").isNumber())
            .andExpect(jsonPath("$.party_id").isNumber())
            .andExpect(jsonPath("$.date_from").isNotEmpty())
            .andExpect(jsonPath("$.date_to").isNotEmpty())
            .andExpect(jsonPath("$.excluded_date").isNotEmpty());

        long creditorAccountId = objectMapper.readTree(body).get("creditor_account_id").longValue();
        long defendantAccountId = objectMapper.readTree(body).get("defendant_account_id").longValue();
        long partyId = objectMapper.readTree(body).get("party_id").longValue();

        assertThat(count("creditor_accounts", "creditor_account_id = ?", creditorAccountId)).isEqualTo(1);
        assertThat(count("defendant_accounts", "defendant_account_id = ?", defendantAccountId)).isEqualTo(1);
        assertThat(count("parties", "party_id = ?", partyId)).isEqualTo(1);
        assertThat(count("impositions", "creditor_account_id = ?", creditorAccountId)).isEqualTo(1);
        assertThat(count("creditor_transactions", "creditor_account_id = ?", creditorAccountId)).isEqualTo(3);
        assertThat(count(
            "amendments",
            "associated_record_type = 'creditor_accounts' AND associated_record_id = ?",
            String.valueOf(creditorAccountId)
        )).isEqualTo(3);
        assertThat(count(
            "notes",
            "associated_record_type = 'creditor_accounts' AND associated_record_id = ?",
            String.valueOf(creditorAccountId)
        )).isEqualTo(3);

        mockMvc.perform(delete(URL_BASE + "/" + creditorAccountId))
            .andExpect(status().isNoContent());

        assertThat(count("creditor_accounts", "creditor_account_id = ?", creditorAccountId)).isZero();
        assertThat(count("defendant_accounts", "defendant_account_id = ?", defendantAccountId)).isZero();
        assertThat(count("parties", "party_id = ?", partyId)).isZero();
        assertThat(count("impositions", "creditor_account_id = ?", creditorAccountId)).isZero();
        assertThat(count("creditor_transactions", "creditor_account_id = ?", creditorAccountId)).isZero();
        assertThat(count(
            "amendments",
            "associated_record_type = 'creditor_accounts' AND associated_record_id = ?",
            String.valueOf(creditorAccountId)
        )).isZero();
        assertThat(count(
            "notes",
            "associated_record_type = 'creditor_accounts' AND associated_record_id = ?",
            String.valueOf(creditorAccountId)
        )).isZero();
    }

    private int count(String table, String whereClause, Object value) {
        return Objects.requireNonNull(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM " + table + " WHERE " + whereClause,
            Integer.class,
            value
        ));
    }
}
