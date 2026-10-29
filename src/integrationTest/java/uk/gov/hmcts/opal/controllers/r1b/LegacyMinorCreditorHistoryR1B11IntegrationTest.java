package uk.gov.hmcts.opal.controllers.r1b;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.service.opal.MinorCreditorHistoryPdplLoggingService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@Tag("ExtendedTest")
@ActiveProfiles({"integration", "legacy"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b-1-1=true"
})
@DisplayName("Legacy Minor Creditor History R1B11 Integration Test")
@Slf4j(topic = "opal.LegacyMinorCreditorHistoryR1B11IntegrationTest")
public class LegacyMinorCreditorHistoryR1B11IntegrationTest extends AbstractIntegrationTest {

    private static final String HISTORY_URL = "/minor-creditor-accounts/{accountId}/history";

    private static final long MINOR_CREDITOR_ID = 99000000000808L;

    @MockitoBean
    private MinorCreditorHistoryPdplLoggingService historyPdplLoggingService;

    @Test
    @JiraEpic("PO-2653")
    @JiraStory("PO-10875")
    @DisplayName("LEGACY: Returns all supported item types when no item types are specified")
    void getHistory_whenNoItemTypesSpecified_returnsAllSupportedItemTypes() throws Exception {
        ResultActions result = mockMvc.perform(get(HISTORY_URL, MINOR_CREDITOR_ID)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()));

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.historyItems", hasSize(7)))
            .andExpect(jsonPath("$.historyItems[*].type", hasItems("Financial", "Note", "Amendment")));
    }

    @Test
    @JiraEpic("PO-2653")
    @JiraStory("PO-10875")
    @DisplayName("LEGACY: Returns all supported item types when all item types are specified")
    void getHistory_whenAllItemTypesSpecified_returnsAllSupportedItemTypes() throws Exception {
        ResultActions result = mockMvc.perform(get(HISTORY_URL, MINOR_CREDITOR_ID)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken())
            .param("itemTypes", "amendment", "note", "financial"));

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.historyItems", hasSize(7)))
            .andExpect(jsonPath("$.historyItems[*].type", hasItems("Financial", "Note", "Amendment")));
    }

    @Test
    @JiraEpic("PO-2653")
    @JiraStory("PO-10875")
    @DisplayName("LEGACY: Returns only financial history items when FINANCIAL item type is specified")
    void getHistory_whenFinancialItemTypeSpecified_returnsOnlyFinancialItems() throws Exception {
        ResultActions result = mockMvc.perform(get(HISTORY_URL, MINOR_CREDITOR_ID)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken())
            .param("dateFrom", "2026-05-01")
            .param("dateTo", "2026-05-31")
            .param("itemTypes", "financial"));

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.historyItems", hasSize(2)))
            .andExpect(jsonPath("$.historyItems[*].type", everyItem(is("Financial"))));
    }
}
