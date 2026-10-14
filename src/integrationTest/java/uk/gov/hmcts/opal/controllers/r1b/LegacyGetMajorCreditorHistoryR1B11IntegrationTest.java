package uk.gov.hmcts.opal.controllers.r1b;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "legacy"})
@TestPropertySource(properties = {"launchdarkly.enabled=false", "launchdarkly.default-flag-values.release-1b-1-1=true"})
@DisplayName("Legacy Get Major Creditor History Release-1B-1-1 Integration Test")
public class LegacyGetMajorCreditorHistoryR1B11IntegrationTest extends AbstractIntegrationTest {

    private static final String URL = "/major-creditor-accounts/{id}/history";

    @Test
    @JiraEpic("PO-2450")
    @JiraStory("PO-10876")
    @DisplayName("LEGACY: Returns all supported item types when no item types are specified")
    void getHistory_whenNoItemTypesSpecified_returnsAllSupportedItemTypes() throws Exception {
        ResultActions result = mockMvc.perform(get(URL, 10770000000041L)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken())
            .param("dateFrom", "2022-01-05")
            .param("dateTo", "2026-10-31"));

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.historyItems", hasSize(3)))
            .andExpect(jsonPath("$.historyItems[*].type", hasItems("Financial", "Note")));
    }

    @Test
    @JiraEpic("PO-2450")
    @JiraStory("PO-10876")
    @DisplayName("LEGACY: Returns all supported item types when all item types are specified")
    void getHistory_whenAllItemTypesSpecified_returnsAllSupportedItemTypes() throws Exception {
        ResultActions result = mockMvc.perform(get(URL, 10770000000041L)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken())
            .param("dateFrom", "2022-01-05")
            .param("dateTo", "2026-10-31")
            .param("itemTypes", "Note", "Financial"));

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.historyItems", hasSize(3)))
            .andExpect(jsonPath("$.historyItems[*].type", hasItems("Note", "Financial")));
    }

    @Test
    @JiraEpic("PO-2450")
    @JiraStory("PO-10876")
    @DisplayName("LEGACY: Returns only note history items when NOTE item type is specified")
    void getHistory_whenNoteItemTypeSpecified_returnsOnlyNoteItems() throws Exception {
        ResultActions result = mockMvc.perform(get(URL, 10770000000042L)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken())
            .param("dateFrom", "2022-01-05")
            .param("dateTo", "2026-10-31")
            .param("itemTypes", "Note"));

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.historyItems", hasSize(1)))
            .andExpect(jsonPath("$.historyItems[*].type", everyItem(is("Note"))));
    }

    @Test
    @JiraEpic("PO-2450")
    @JiraStory("PO-10876")
    @DisplayName("LEGACY: Returns only financial history items when Financial item type is specified")
    void getHistory_whenFinancialItemTypeSpecified_returnsOnlyFinancialItems() throws Exception {
        ResultActions result = mockMvc.perform(get(URL, 10770000000043L)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken())
            .param("dateFrom", "2022-01-05")
            .param("dateTo", "2026-10-31")
            .param("itemTypes", "Financial"));

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.historyItems", hasSize(2)))
            .andExpect(jsonPath("$.historyItems[*].type", everyItem(is("Financial"))));
    }
}
