package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Slf4j(topic = "opal.OpalDefendantsFixedPenaltyIntegrationTest")
class OpalDefendantsFixedPenaltyIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    @Test
    @DisplayName("OPAL: Get Defendant Account Fixed Penalty [@PO-1819]")
    void testGetDefendantAccountFixedPenalty() throws Exception {
        authoriseAllPermissions();

        ResultActions actions = mockMvc.perform(
            get("/defendant-accounts/77/fixed-penalty").header("authorization", "Bearer some_value"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDefendantAccountFixedPenalty: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("etag", matchesPattern("\"\\d+\"")))
            .andExpect(jsonPath("$.vehicle_fixed_penalty_flag").value(true))
            .andExpect(jsonPath("$.fixed_penalty_ticket_details.issuing_authority")
                .value("Kingston-upon-Thames Mags Court"))
            .andExpect(jsonPath("$.fixed_penalty_ticket_details.ticket_number").value("888"))
            .andExpect(jsonPath("$.fixed_penalty_ticket_details.place_of_offence").value("London"))
            .andExpect(jsonPath("$.vehicle_fixed_penalty_details.vehicle_registration_number").value("AB12CDE"))
            .andExpect(jsonPath("$.vehicle_fixed_penalty_details.vehicle_drivers_license").value("DOE1234567"))
            .andExpect(jsonPath("$.vehicle_fixed_penalty_details.notice_number").value("PN98765"))
            .andExpect(jsonPath("$.vehicle_fixed_penalty_details.date_notice_issued").exists());

        jsonSchemaValidationService.validateOrError(body, DEFENDANT_FIXED_PENALTY_RESPONSE_SCHEMA);
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account Fixed Penalty - 404 when not found [@PO-1819]")
    void testGetDefendantAccountFixedPenalty_NotFound() throws Exception {
        authoriseAllPermissions();

        ResultActions actions = mockMvc.perform(
            get("/defendant-accounts/99999/fixed-penalty").header("authorization", "Bearer some_value"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDefendantAccountFixedPenalty_NotFound: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"))
            .andExpect(jsonPath("$.title").value("Entity Not Found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("The requested entity could not be found"))
            .andExpect(header().doesNotExist("ETag"));
    }
}
