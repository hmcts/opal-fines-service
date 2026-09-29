package uk.gov.hmcts.opal.controllers.r1b;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.shared.util.OpenApiContractAssertions.assertGet200JsonResponseMatchesBundledSpec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "opal"})
@DisplayName("OPAL Defendant Account Impositions Integration Tests")
@Sql(
    scripts = "classpath:db/insertData/insert_into_impositions_entity_graph.sql",
    executionPhase = BEFORE_TEST_CLASS
)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_impositions_entity_graph.sql",
    executionPhase = AFTER_TEST_CLASS
)
class OpalDefendantAccountImpositionsIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/defendant-accounts";
    private static final String CREDITOR_ACCOUNT_TYPE =
        "$.impositions[0].creditor.creditor_account_type_reference.creditor_account_type";
    private static final String CREDITOR_ACCOUNT_DISPLAY_NAME =
        "$.impositions[0].creditor.creditor_account_type_reference.creditor_account_display_name";

    @Test
    @DisplayName("OPAL: Get Defendant Account Impositions returns major creditor imposition with schema-valid body")
    @JiraStory("PO-2077")
    @JiraEpic("PO-979")
    @JiraTestKey("PO-7638")
    void getImpositions_returnsMajorCreditorImposition() throws Exception {
        MvcResult result = performGetImpositions(551002L)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"7\""))
            .andExpect(jsonPath("$.impositions", hasSize(1)))
            .andExpect(jsonPath("$.impositions[0].date_added").value("2026-04-17"))
            .andExpect(jsonPath("$.impositions[0].imposition.result_id").value("IGR001"))
            .andExpect(jsonPath("$.impositions[0].imposition.result_title").value("Imposition Graph Result"))
            .andExpect(jsonPath("$.impositions[0].creditor.creditor_account_id").value(551004))
            .andExpect(jsonPath(CREDITOR_ACCOUNT_TYPE).value("MJ"))
            .andExpect(jsonPath(CREDITOR_ACCOUNT_DISPLAY_NAME).value("Major Creditor"))
            .andExpect(jsonPath("$.impositions[0].creditor.major_creditor_name").value("Graph Major Creditor"))
            .andExpect(jsonPath("$.impositions[0].creditor.minor_creditor_organisation_flag")
                .value(is(nullValue())))
            .andExpect(jsonPath("$.impositions[0].creditor.individual_name").value(is(nullValue())))
            .andExpect(jsonPath("$.impositions[0].creditor.company_name").value(is(nullValue())))
            .andExpect(jsonPath("$.impositions[0].imposed_amount").value(250.00))
            .andExpect(jsonPath("$.impositions[0].paid_amount").value(25.00))
            .andExpect(jsonPath("$.impositions[0].balance").value(225.00))
            .andExpect(jsonPath("$.impositions[0].date_imposed").value("2026-04-16"))
            .andExpect(jsonPath("$.impositions[0].offence.offence_id").value(5510))
            .andExpect(jsonPath("$.impositions[0].offence.cjs_code").value("IG5510"))
            .andExpect(jsonPath("$.impositions[0].offence.offence_title").value("Imposition Graph Offence"))
            .andExpect(jsonPath("$.impositions[0].imposed_by.court_id").value(551001))
            .andExpect(jsonPath("$.impositions[0].imposed_by.court_code").value(101))
            .andExpect(jsonPath("$.impositions[0].imposed_by.court_name").value("Graph Test Court"))
            .andExpect(jsonPath("$.impositions[0].imposition_id").value(551005))
            .andReturn();

        assertGet200JsonResponseMatchesBundledSpec(
            ToJsonString.toJsonNode(result.getResponse().getContentAsString()),
            "/defendant-accounts/{id}/impositions"
        );
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account Impositions returns minor creditor imposition")
    @JiraStory("PO-2077")
    @JiraEpic("PO-979")
    @JiraTestKey("PO-7635")
    void getImpositions_returnsMinorCreditorImposition() throws Exception {
        MvcResult result = performGetImpositions(551008L)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"8\""))
            .andExpect(jsonPath("$.impositions", hasSize(1)))
            .andExpect(jsonPath("$.impositions[0].creditor.creditor_account_id").value(551007))
            .andExpect(jsonPath(CREDITOR_ACCOUNT_TYPE).value("MN"))
            .andExpect(jsonPath(CREDITOR_ACCOUNT_DISPLAY_NAME).value("Minor Creditor"))
            .andExpect(jsonPath("$.impositions[0].creditor.major_creditor_name").value(is(nullValue())))
            .andExpect(jsonPath("$.impositions[0].creditor.minor_creditor_organisation_flag").value(false))
            .andExpect(jsonPath("$.impositions[0].creditor.individual_name.forenames").value("Creditor"))
            .andExpect(jsonPath("$.impositions[0].creditor.individual_name.surname").value("Minor"))
            .andExpect(jsonPath("$.impositions[0].creditor.company_name").value(is(nullValue())))
            .andExpect(jsonPath("$.impositions[0].imposed_amount").value(80.00))
            .andExpect(jsonPath("$.impositions[0].paid_amount").value(30.00))
            .andExpect(jsonPath("$.impositions[0].balance").value(50.00))
            .andExpect(jsonPath("$.impositions[0].imposed_by").value(is(nullValue())))
            .andExpect(jsonPath("$.impositions[0].imposition_id").value(551009))
            .andReturn();

        assertGet200JsonResponseMatchesBundledSpec(
            ToJsonString.toJsonNode(result.getResponse().getContentAsString()),
            "/defendant-accounts/{id}/impositions"
        );
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account Impositions returns empty list for account without impositions")
    @JiraStory("PO-2077")
    @JiraEpic("PO-979")
    @JiraTestKey("PO-7636")
    void getImpositions_whenNoImpositions_returnsEmptyList() throws Exception {
        MvcResult result = performGetImpositions(551010L)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"9\""))
            .andExpect(jsonPath("$.impositions", hasSize(0)))
            .andReturn();

        assertGet200JsonResponseMatchesBundledSpec(
            ToJsonString.toJsonNode(result.getResponse().getContentAsString()),
            "/defendant-accounts/{id}/impositions"
        );
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account Impositions returns 404 for missing account")
    @JiraStory("PO-2077")
    @JiraEpic("PO-979")
    @JiraTestKey("PO-7637")
    void getImpositions_whenAccountDoesNotExist_returnsNotFound() throws Exception {
        performGetImpositions(559999L)
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"))
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account Impositions returns 403 when user lacks permission")
    @JiraStory("PO-2077")
    @JiraEpic("PO-979")
    @JiraTestKey("PO-7634")
    void getImpositions_whenUserLacksPermission_returnsForbidden() throws Exception {
        userStateStub.setupWithNoPermissions();
        performGetImpositions(551002L)
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.detail").value("You do not have permission to access this resource"));
    }

    private ResultActions performGetImpositions(Long defendantAccountId) throws Exception {
        return mockMvc.perform(get(URL_BASE + "/" + defendantAccountId + "/impositions")
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header("Authorization", userStateStub.getBearerToken())
            .accept(MediaType.APPLICATION_JSON));
    }
}
