package uk.gov.hmcts.opal.controllers.r1a;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.DraftAccountService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@Slf4j(topic = "opal.DraftAccountControllerDeleteIntegrationTest")
@DisplayName("DraftAccountControllerDeleteIntegrationTest")
class DraftAccountControllerDeleteIntegrationTest extends CommonDraftAccountControllerIntegrationTest {

    @Test
    @DisplayName("Delete draft account - should log successful security event")
    @JiraStory("PO-2570")
    @JiraEpic("PO-2808")
    void testDeleteDraftAccount_logsSecurityEventSuccess() throws Exception {
        Long draftAccountId = 6L;

        ResultActions resultActions = mockMvc.perform(delete(URL_BASE + "/" + draftAccountId)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header("authorization", userStateStub.getBearerToken()));

        String response = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testDeleteDraftAccount_logsSecurityEventSuccess: Response body:\n{}",
            ToJsonString.toPrettyJson(response));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json("""
                { "message": "Draft Account '6' deleted"}
                """));

        verify(securityEventLoggingService, times(1)).logEvent(
            eq(DraftAccountService.EVENT_ACCOUNT_DELETION),
            eq("Success"),
            eq((short) 78),
            eq("Deletion"),
            any(LocalDateTime.class),
            eq(Map.of(
                "UserIdentifier", 500000000L,
                "DraftAccountIdentifier", draftAccountId,
                "DraftAccountSubmittedByUserIdentifier", "user_003"
            ))
        );
    }
}
