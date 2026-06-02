package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.SEARCH_AND_VIEW_ACCOUNTS;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
@Sql(scripts = "classpath:db/insertData/insert_into_central_funds.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_central_funds.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.CentralFundControllerIntegrationTest")
@DisplayName("Central Fund Controller Integration Tests")
class CentralFundControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/central-funds";
    private static final String AUTH_HEADER = "Bearer test-token";

    @MockitoBean
    private UserStateService userStateService;

    @BeforeEach
    void setup() {
        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(UserStateUtil.permissionUser((short) 73, SEARCH_AND_VIEW_ACCOUNTS));
    }

    @Test
    @DisplayName("PO-2320: GET central fund returns 200 with payload and ETag")
    @JiraStory("PO-2320")
    @JiraEpic("PO-1286")
    void getCentralFund_returnsPayloadWithEtag() throws Exception {
        Map<String, Object> centralFund = getCentralFundRow(73);

        ResultActions actions = mockMvc.perform(get(URL_BASE + "/73")
            .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        MockHttpServletResponse response = actions.andReturn().getResponse();
        String body = response.getContentAsString();
        log.info(":getCentralFund_returnsPayloadWithEtag: Response body:\n{}", ToJsonString.toPrettyJson(body));

        assertEquals("\"" + longValue(centralFund, "version_number") + "\"", response.getHeader(HttpHeaders.ETAG));
        assertCentralFundResponse(objectMapper.readTree(body), centralFund);
        assertFalse(Boolean.TRUE.equals(centralFund.get("welsh_language")),
            "Seeded central fund data should exercise the default welsh speaking mapping");
    }

    @Test
    @DisplayName("PO-2320: GET central fund returns 404 when central fund does not exist")
    @JiraStory("PO-2320")
    @JiraEpic("PO-1286")
    void getCentralFund_whenCentralFundDoesNotExist_returnsNotFound() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE + "/999").header(HttpHeaders.AUTHORIZATION, AUTH_HEADER));

        actions.andExpect(status().isNotFound())
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
    @DisplayName("PO-2320: GET central fund returns 401 when auth header is missing")
    @JiraStory("PO-2320")
    @JiraEpic("PO-1286")
    void getCentralFund_whenAuthHeaderMissing_returnsUnauthorized() throws Exception {
        doThrow(new ResponseStatusException(UNAUTHORIZED, "Unauthorized"))
            .when(userStateService).checkForAuthorisedUser(any());

        mockMvc.perform(get(URL_BASE + "/73"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail").value("Unauthorized"))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @Test
    @DisplayName("PO-2320: GET central fund returns 401 when token is invalid")
    @JiraStory("PO-2320")
    @JiraEpic("PO-1286")
    void getCentralFund_whenTokenInvalid_returnsUnauthorized() throws Exception {
        doThrow(new ResponseStatusException(UNAUTHORIZED, "Invalid token"))
            .when(userStateService).checkForAuthorisedUser(any());

        mockMvc.perform(get(URL_BASE + "/73")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid_token"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail").value("Invalid token"))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @Test
    @DisplayName("PO-2320: GET central fund returns 403 when user lacks Search and View Accounts")
    @JiraStory("PO-2320")
    @JiraEpic("PO-1286")
    void getCentralFund_whenUserLacksPermission_returnsForbidden() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.noPermissionsUser());

        mockMvc.perform(get(URL_BASE + "/73").header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"));
    }

    @Test
    @DisplayName("PO-2320: GET central fund returns identical payload and headers for consecutive requests")
    @JiraStory("PO-2320")
    @JiraEpic("PO-1286")
    void getCentralFund_whenDataUnchanged_returnsIdenticalPayloadAndHeaders() throws Exception {
        ResultActions first = mockMvc.perform(get(URL_BASE + "/73")
            .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER));
        ResultActions second = mockMvc.perform(get(URL_BASE + "/73")
            .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER));

        first.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        second.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        MockHttpServletResponse firstResponse = first.andReturn().getResponse();
        MockHttpServletResponse secondResponse = second.andReturn().getResponse();

        assertEquals(firstResponse.getContentAsString(), secondResponse.getContentAsString());
        assertEquals(firstResponse.getContentType(), secondResponse.getContentType());
        assertEquals(firstResponse.getHeader(HttpHeaders.ETAG), secondResponse.getHeader(HttpHeaders.ETAG));
        assertEquals(firstResponse.getHeaderNames(), secondResponse.getHeaderNames());
    }

    private Map<String, Object> getCentralFundRow(int businessUnitId) {
        return jdbcTemplate.queryForMap("""
            SELECT ca.creditor_account_id AS creditor_account_id,
                   ca.account_number AS account_number,
                   ci.item_values ->> 'name' AS name,
                   bu.business_unit_id AS business_unit_id,
                   bu.business_unit_name AS business_unit_name,
                   bu.welsh_language AS welsh_language,
                   ca.version_number AS version_number
              FROM creditor_accounts ca
              JOIN business_units bu
                ON bu.business_unit_id = ca.business_unit_id
              JOIN configuration_items ci
                ON ci.business_unit_id = bu.business_unit_id
               AND ci.item_name = 'CENTRAL_FUND_ACCOUNT'
             WHERE ca.business_unit_id = ?
               AND ca.creditor_account_type = 'CF'::public.t_creditor_account_type_enum
             ORDER BY ca.creditor_account_id
             LIMIT 1
            """, businessUnitId);
    }

    private void assertCentralFundResponse(JsonNode response, Map<String, Object> centralFund) {
        assertEquals(Set.of("major_creditor", "business_unit_details"), fieldNames(response));

        JsonNode majorCreditor = response.get("major_creditor");
        assertEquals(Set.of("creditor_account_id", "account_number", "name"), fieldNames(majorCreditor));
        assertEquals(longValue(centralFund, "creditor_account_id"), majorCreditor.get("creditor_account_id").asLong());
        assertEquals(centralFund.get("account_number"), majorCreditor.get("account_number").asText());
        assertEquals(centralFund.get("name"), majorCreditor.get("name").asText());

        JsonNode businessUnitDetails = response.get("business_unit_details");
        assertEquals(Set.of("business_unit_id", "business_unit_name", "welsh_speaking"), fieldNames(businessUnitDetails));
        assertEquals(String.valueOf(longValue(centralFund, "business_unit_id")),
            businessUnitDetails.get("business_unit_id").asText());
        assertEquals(centralFund.get("business_unit_name"), businessUnitDetails.get("business_unit_name").asText());
        assertEquals(Boolean.TRUE.equals(centralFund.get("welsh_language")) ? "Y" : "N",
            businessUnitDetails.get("welsh_speaking").asText());
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

    private Set<String> fieldNames(JsonNode node) {
        Set<String> fields = new HashSet<>();
        fields.addAll(node.propertyNames());
        return fields;
    }

    private long longValue(Map<String, Object> values, String key) {
        return ((Number) values.get(key)).longValue();
    }
}
