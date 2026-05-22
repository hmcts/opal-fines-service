package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.dto.ToJsonString;

@ActiveProfiles(profiles = {"integration-with-spring-security", "legacy"}, inheritProfiles = false)
@Slf4j(topic = "opal.LegacyDraftAccountIntegrationTest")
@DisplayName("DraftAccountController Integration Tests (LEGACY)")
public class LegacyDraftAccountIntegrationTest extends CommonDraftAccountControllerIntegrationTest {

    @Test
    @DisplayName("Publish draft account does not return legacy server error in response [@PO-2819]")
    void testPublishDraftAccountGobServerErrorIsNotReturnedInResponse() throws Exception {

        long draftAccountId = 3L;
        authoriseDeveloperUserForAllDraftTestBusinessUnits();

        ResultActions actions = mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
            .header("authorization", "Bearer " + validToken)
            .header("If-Match", getIfMatchForDraftAccount(draftAccountId))
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("73", "Publishing Pending","A")));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().string(not(containsString("Result FCOST is not valid")))) // =server error msg
            .andExpect(content().string(containsString("An error was encountered during publication of the account")));
    }

}
