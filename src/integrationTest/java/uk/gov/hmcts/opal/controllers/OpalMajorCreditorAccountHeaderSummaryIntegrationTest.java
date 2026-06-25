package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.SEARCH_AND_VIEW_ACCOUNTS;

import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "opal"})
@DisplayName("Major Creditor Account Header Summary Opal Integration Tests")
@Slf4j(topic = "opal.OpalMajorCreditorAccountHeaderSummaryIntegrationTest")
class OpalMajorCreditorAccountHeaderSummaryIntegrationTest extends AbstractIntegrationTest {

    private static final String URL = "/major-creditor-accounts/{id}/header-summary";

    @Test
    @DisplayName("PO-2131 INT.01 to INT.04 - Opal valid request returns mapped body and ETag")
    @JiraStory("PO-2131")
    @JiraEpic("PO-1286")
    @JiraTestKey("PO-7651")
    void getHeaderSummary_successReturnsMappedResponseAndEtag() throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 77, SEARCH_AND_VIEW_ACCOUNTS);

        ResultActions resultActions = mockMvc.perform(get(URL, 10770000000041L)
            .accept(MediaType.APPLICATION_JSON)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()));

        MockHttpServletResponse response = resultActions.andReturn().getResponse();
        String body = response.getContentAsString();
        log.info(":getHeaderSummary_successReturnsMappedResponseAndEtag: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string(HttpHeaders.ETAG, "\"1\""));

        assertHeaderSummaryResponse(body);
    }

    @Test
    @DisplayName("PO-2131 INT.05 - Opal repeated request returns consistent body and ETag")
    @JiraStory("PO-2131")
    @JiraEpic("PO-1286")
    void getHeaderSummary_repeatedRequestReturnsConsistentResponse() throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 77, SEARCH_AND_VIEW_ACCOUNTS);

        ResultActions first = mockMvc.perform(get(URL, 10770000000041L)
            .accept(MediaType.APPLICATION_JSON)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()));
        ResultActions second = mockMvc.perform(get(URL, 10770000000041L)
            .accept(MediaType.APPLICATION_JSON)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()));

        first.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON));
        second.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON));

        MockHttpServletResponse firstResponse = first.andReturn().getResponse();
        MockHttpServletResponse secondResponse = second.andReturn().getResponse();

        assertEquals(firstResponse.getContentAsString(), secondResponse.getContentAsString());
        assertEquals(firstResponse.getHeader(HttpHeaders.ETAG), secondResponse.getHeader(HttpHeaders.ETAG));
    }

    @Test
    @DisplayName("PO-2131 INT.06 - Opal permission in matching business unit returns 200")
    @JiraStory("PO-2131")
    @JiraEpic("PO-1286")
    void getHeaderSummary_permissionInMatchingBusinessUnitReturns200() throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 77, SEARCH_AND_VIEW_ACCOUNTS);

        mockMvc.perform(get(URL, 10770000000041L)
                .accept(MediaType.APPLICATION_JSON)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("PO-2131 INT.07 - Opal valid token without permission returns 403")
    @JiraStory("PO-2131")
    @JiraEpic("PO-1286")
    void getHeaderSummary_withoutPermissionReturns403() throws Exception {
        userStateStub.setupWithNoPermissions();

        mockMvc.perform(get(URL, 10770000000041L)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("PO-2131 INT.07 - Opal permission in non-matching business unit returns 403")
    @JiraStory("PO-2131")
    @JiraEpic("PO-1286")
    void getHeaderSummary_permissionInDifferentBusinessUnitReturns403() throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 10, SEARCH_AND_VIEW_ACCOUNTS);

        mockMvc.perform(get(URL, 10770000000041L)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("PO-2131 INT.09 - Opal missing major creditor account returns 404")
    @JiraStory("PO-2131")
    @JiraEpic("PO-1286")
    @JiraTestKey("PO-7650")
    void getHeaderSummary_notFoundReturns404() throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 77, SEARCH_AND_VIEW_ACCOUNTS);

        ResultActions actions = mockMvc.perform(get(URL, 999999L)
                .accept(MediaType.APPLICATION_JSON)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(header().doesNotExist(HttpHeaders.ETAG))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"))
            .andExpect(jsonPath("$.title").value("Entity Not Found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("The requested entity could not be found"))
            .andExpect(jsonPath("$.retriable").value(false))
            .andExpect(jsonPath("$.major_creditor").doesNotExist())
            .andExpect(jsonPath("$.business_unit_details").doesNotExist());

        JsonNode problem = objectMapper.readTree(actions.andReturn().getResponse().getContentAsString());
        assertStandardProblemResponse(problem,
            404,
            "Entity Not Found",
            "The requested entity could not be found",
            "https://hmcts.gov.uk/problems/entity-not-found");
    }

    @Test
    @DisplayName("PO-2131 INT.08 - Opal missing token returns 403")
    @JiraStory("PO-2131")
    @JiraEpic("PO-1286")
    void getHeaderSummary_missingTokenReturnsForbidden() throws Exception {
        userStateStub.setupWithNoPermissions();

        mockMvc.perform(get(URL, 10770000000041L)
                .accept(MediaType.APPLICATION_JSON)
                .with(userStateStub.getInvalidAuthenticaitonRequestPostProcessor()))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail").value("You do not have permission to access this resource"))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @Test
    @DisplayName("PO-2131 INT.08 - Opal invalid token returns 403")
    @JiraStory("PO-2131")
    @JiraEpic("PO-1286")
    void getHeaderSummary_invalidTokenReturnsForbidden() throws Exception {
        mockMvc.perform(get(URL, 10770000000041L)
                .accept(MediaType.APPLICATION_JSON)
                .with(userStateStub.getInvalidAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid_token"))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail").value("You do not have permission to access this resource"))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    private void assertHeaderSummaryResponse(String body) throws Exception {
        JsonNode response = objectMapper.readTree(body);

        assertEquals(Set.of("major_creditor", "awaiting_payout", "business_unit_details"), fieldNames(response));

        JsonNode majorCreditor = response.get("major_creditor");
        assertEquals(
            Set.of("creditor_account_id", "account_number", "name", "account_reference"),
            fieldNames(majorCreditor)
        );
        assertEquals(10770000000041L, majorCreditor.get("creditor_account_id").asLong());
        assertEquals("00001235G", majorCreditor.get("account_number").asText());
        assertEquals("TFL2 ATCM Testing", majorCreditor.get("name").asText());

        JsonNode accountReference = majorCreditor.get("account_reference");
        assertEquals(Set.of("account_type", "display_name"), fieldNames(accountReference));
        assertEquals("MJ", accountReference.get("account_type").asText());
        assertEquals("Major Creditor", accountReference.get("display_name").asText());

        JsonNode businessUnitDetails = response.get("business_unit_details");
        assertEquals(
            Set.of("business_unit_id", "business_unit_name", "welsh_speaking"),
            fieldNames(businessUnitDetails)
        );
        assertEquals("77", businessUnitDetails.get("business_unit_id").asText());
        assertEquals("Camberwell Green", businessUnitDetails.get("business_unit_name").asText());
        assertEquals("N", businessUnitDetails.get("welsh_speaking").asText());

        assertEquals(0, response.get("awaiting_payout").decimalValue().intValueExact());
    }

    private Set<String> fieldNames(JsonNode node) {
        Set<String> fields = new HashSet<>();
        fields.addAll(node.propertyNames());
        return fields;
    }

    private void assertStandardProblemResponse(JsonNode problem,
                                               int statusCode,
                                               String title,
                                               String detail,
                                               String type) {
        assertEquals(Set.of("type", "title", "status", "detail", "instance", "operation_id", "retriable", "reason"),
            fieldNames(problem));
        assertEquals(statusCode, problem.get("status").asInt());
        assertEquals(title, problem.get("title").asText());
        assertEquals(detail, problem.get("detail").asText());
        assertEquals(type, problem.get("type").asText());
        assertFalse(problem.get("retriable").asBoolean());
        assertTrue(problem.get("reason").asText().matches(".+"));
        assertTrue(problem.get("operation_id").asText().matches(".+"));
        assertTrue(problem.get("instance").asText().matches("https://hmcts.gov.uk/problems/instance/.+"));
    }
}
