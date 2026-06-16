package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.SchemaPaths.GET_DEFENDANT_ACCOUNT_IMPOSITIONS_RESPONSE;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "legacy"})
@DisplayName("Legacy Defendant Account Impositions Integration Tests")
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
class LegacyDefendantAccountImpositionsIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/defendant-accounts";
    private static final String AUTH_HEADER = "Bearer test-token";

    @MockitoBean
    private UserStateService userStateService;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @MockitoBean
    private UserStateClientService userStateClientService;

    @MockitoSpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    @BeforeEach
    void setupUserState() {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allPermissionsUser());
    }

    @Test
    @DisplayName("LEGACY: Get Defendant Account Impositions returns schema-valid imposition response")
    @JiraStory("PO-2078")
    @JiraEpic("PO-979")
    void getImpositions_returnsLegacyImpositionResponse() throws Exception {
        MvcResult result = performGetImpositions(12345L)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"1\""))
            .andExpect(jsonPath("$.impositions", hasSize(1)))
            .andExpect(jsonPath("$.impositions[0].date_added").value("2026-05-06"))
            .andExpect(jsonPath("$.impositions[0].imposition.result_id").value("ABDC"))
            .andExpect(jsonPath("$.impositions[0].imposition.result_title")
                .value("Application made for Benefit Deductions"))
            .andExpect(jsonPath("$.impositions[0].creditor.creditor_account_id").value(99000000000806L))
            .andExpect(jsonPath("$.impositions[0].creditor.account_type").value("MN"))
            .andExpect(jsonPath("$.impositions[0].creditor.display_name").value("Minor Creditor"))
            .andExpect(jsonPath("$.impositions[0].creditor.minor_creditor_party_id").value(99000000000906L))
            .andExpect(jsonPath("$.impositions[0].creditor.name").value("Metropolitan Traffic Unit"))
            .andExpect(jsonPath("$.impositions[0].imposed_amount").value(600.00))
            .andExpect(jsonPath("$.impositions[0].paid_amount").value(60.00))
            .andExpect(jsonPath("$.impositions[0].balance").value(540.00))
            .andExpect(jsonPath("$.impositions[0].offence.code").value("OFF0006"))
            .andExpect(jsonPath("$.impositions[0].offence.title").value("Test Offence 6"))
            .andExpect(jsonPath("$.impositions[0].imposition_id").value(99000000003006L))
            .andReturn();

        jsonSchemaValidationService.validateOrError(
            result.getResponse().getContentAsString(),
            GET_DEFENDANT_ACCOUNT_IMPOSITIONS_RESPONSE
        );
    }

    @Test
    @DisplayName("LEGACY: Get Defendant Account Impositions returns 403 when user lacks permission")
    @JiraStory("PO-2078")
    @JiraEpic("PO-979")
    void getImpositions_whenUserLacksPermission_returnsForbidden() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.noPermissionsUser());

        performGetImpositions(12345L)
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"))
            .andExpect(jsonPath("$.status").value(403));
    }

    private ResultActions performGetImpositions(Long defendantAccountId) throws Exception {
        return mockMvc.perform(get(URL_BASE + "/" + defendantAccountId + "/impositions")
                                   .header("Authorization", AUTH_HEADER)
                                   .accept(MediaType.APPLICATION_JSON));
    }
}
