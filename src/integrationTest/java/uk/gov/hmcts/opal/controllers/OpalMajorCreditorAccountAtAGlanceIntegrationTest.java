package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
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

import jakarta.persistence.QueryTimeoutException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.repository.MajorCreditorAccountAtAGlanceRepository;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import org.yaml.snakeyaml.Yaml;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
@Sql(
    scripts = "classpath:db/insertData/insert_into_major_creditor_at_a_glance_postcode.sql",
    executionPhase = BEFORE_TEST_CLASS
)
@Sql(
    scripts = "classpath:db/insertData/insert_into_major_creditor_at_a_glance_central_fund.sql",
    executionPhase = BEFORE_TEST_CLASS
)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_major_creditor_at_a_glance_postcode.sql",
    executionPhase = AFTER_TEST_CLASS
)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_major_creditor_at_a_glance_central_fund.sql",
    executionPhase = AFTER_TEST_CLASS
)
@DisplayName("Major Creditor Account At A Glance Opal Integration Tests")
@Slf4j(topic = "opal.OpalMajorCreditorAccountAtAGlanceIntegrationTest")
class OpalMajorCreditorAccountAtAGlanceIntegrationTest extends AbstractIntegrationTest {

    private static final String AUTH_HEADER = "Bearer some_value";
    private static final String URL = "/major-creditor-accounts/{id}/at-a-glance";
    private static final long MJ_ACCOUNT_ID = 10770000000041L;
    private static final long CF_ACCOUNT_ID = 77L;

    @MockitoBean
    private UserStateService userStateService;

    @MockitoSpyBean
    private MajorCreditorAccountAtAGlanceRepository majorCreditorAccountAtAGlanceRepository;

    @AfterEach
    void resetSpies() {
        Mockito.reset(majorCreditorAccountAtAGlanceRepository);
    }

    @Test
    @DisplayName("PO-2132 Opal valid MJ request returns mapped body and ETag")
    @JiraStory("PO-2132")
    @JiraEpic("PO-1286")
    @JiraTestKey("PO-7647")
    void getAtAGlance_majorCreditorSuccessReturnsMappedResponseAndEtag() throws Exception {
        when(userStateService.getUserStateV1FromSecurityContext())
            .thenReturn(UserStateUtil.permissionUser((short) 77, SEARCH_AND_VIEW_ACCOUNTS));

        Map<String, Object> account = getAtAGlanceRow(MJ_ACCOUNT_ID);

        ResultActions actions = mockMvc.perform(get(URL, MJ_ACCOUNT_ID)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER));

        MockHttpServletResponse response = actions.andReturn().getResponse();
        String body = response.getContentAsString();
        log.info(":getAtAGlance_majorCreditorSuccessReturnsMappedResponseAndEtag: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
            .andExpect(jsonPath("$.major_creditor.creditor_account_id").value(MJ_ACCOUNT_ID))
            .andExpect(jsonPath("$.major_creditor.name").value(account.get("name")))
            .andExpect(jsonPath("$.major_creditor.code").value(account.get("major_creditor_code")))
            .andExpect(jsonPath("$.major_creditor.address.line_1").value(account.get("address_line_1")))
            .andExpect(jsonPath("$.major_creditor.address.line_2").value(account.get("address_line_2")))
            .andExpect(jsonPath("$.major_creditor.address.line_3").value(account.get("address_line_3")))
            .andExpect(jsonPath("$.major_creditor.address.postcode").value(account.get("postcode")))
            .andExpect(jsonPath("$.major_creditor.pay_by_bacs").value(account.get("pay_by_bacs")));

        JsonNode json = objectMapper.readTree(body);
        assertEquals(Set.of("major_creditor"), fieldNames(json));
        assertEquals(
            Set.of("creditor_account_id", "name", "code", "address", "pay_by_bacs"),
            fieldNames(json.get("major_creditor"))
        );
        assertEquals(
            Set.of("line_1", "line_2", "line_3", "postcode"),
            fieldNames(json.get("major_creditor").get("address"))
        );

        assertMatchesOpenApiSchema(json);
    }

    @Test
    @DisplayName("PO-2132 Opal valid CF request returns mapped body and ETag")
    @JiraStory("PO-2132")
    @JiraEpic("PO-1286")
    @JiraTestKey("PO-7643")
    void getAtAGlance_centralFundSuccessReturnsMappedResponseAndEtag() throws Exception {
        when(userStateService.getUserStateV1FromSecurityContext())
            .thenReturn(UserStateUtil.permissionUser((short) 77, SEARCH_AND_VIEW_ACCOUNTS));

        Map<String, Object> account = getAtAGlanceRow(CF_ACCOUNT_ID);

        ResultActions actions = mockMvc.perform(get(URL, CF_ACCOUNT_ID)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER));

        MockHttpServletResponse response = actions.andReturn().getResponse();
        String body = response.getContentAsString();
        log.info(":getAtAGlance_centralFundSuccessReturnsMappedResponseAndEtag: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
            .andExpect(jsonPath("$.major_creditor.creditor_account_id").value(CF_ACCOUNT_ID))
            .andExpect(jsonPath("$.major_creditor.name").value(account.get("name")))
            .andExpect(jsonPath("$.major_creditor.address.line_1").value(account.get("address_line_1")))
            .andExpect(jsonPath("$.major_creditor.address.line_2").value(account.get("address_line_2")))
            .andExpect(jsonPath("$.major_creditor.address.line_3").value(account.get("address_line_3")))
            .andExpect(jsonPath("$.major_creditor.address.postcode").doesNotExist())
            .andExpect(jsonPath("$.major_creditor.code").doesNotExist())
            .andExpect(jsonPath("$.major_creditor.pay_by_bacs").doesNotExist());

        JsonNode json = objectMapper.readTree(body);
        assertEquals(Set.of("major_creditor"), fieldNames(json));
        assertEquals(Set.of("creditor_account_id", "name", "address"), fieldNames(json.get("major_creditor")));

        assertMatchesOpenApiSchema(json);
    }

    @Test
    @DisplayName("PO-2132 Opal repeated GET returns identical body and ETag")
    @JiraStory("PO-2132")
    @JiraEpic("PO-1286")
    @JiraTestKey("PO-7649")
    void getAtAGlance_repeatedGetReturnsSamePayloadAndHeaders() throws Exception {
        when(userStateService.getUserStateV1FromSecurityContext())
            .thenReturn(UserStateUtil.permissionUser((short) 77, SEARCH_AND_VIEW_ACCOUNTS));

        ResultActions first = mockMvc.perform(get(URL, MJ_ACCOUNT_ID).header(HttpHeaders.AUTHORIZATION, AUTH_HEADER));
        ResultActions second = mockMvc.perform(get(URL, MJ_ACCOUNT_ID).header(HttpHeaders.AUTHORIZATION, AUTH_HEADER));

        MockHttpServletResponse firstResponse = first.andReturn().getResponse();
        MockHttpServletResponse secondResponse = second.andReturn().getResponse();

        assertEquals(firstResponse.getContentAsString(), secondResponse.getContentAsString());
        assertEquals(firstResponse.getHeader(HttpHeaders.ETAG), secondResponse.getHeader(HttpHeaders.ETAG));
        assertEquals(firstResponse.getContentType(), secondResponse.getContentType());
    }

    @Test
    @DisplayName("PO-2132 Opal same BU permission returns 200")
    @JiraStory("PO-2132")
    @JiraEpic("PO-1286")
    @JiraTestKey("PO-7641")
    void getAtAGlance_sameBusinessUnitPermissionReturns200() throws Exception {
        when(userStateService.getUserStateV1FromSecurityContext())
            .thenReturn(UserStateUtil.permissionUser((short) 77, SEARCH_AND_VIEW_ACCOUNTS));

        mockMvc.perform(get(URL, MJ_ACCOUNT_ID).header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PO-2132 Opal different BU permission returns 200")
    @JiraStory("PO-2132")
    @JiraEpic("PO-1286")
    @JiraTestKey("PO-7644")
    void getAtAGlance_differentBusinessUnitPermissionReturns200() throws Exception {
        when(userStateService.getUserStateV1FromSecurityContext())
            .thenReturn(UserStateUtil.permissionUser((short) 73, SEARCH_AND_VIEW_ACCOUNTS));

        mockMvc.perform(get(URL, MJ_ACCOUNT_ID).header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PO-2132 Opal unknown account returns 404 problem response")
    @JiraStory("PO-2132")
    @JiraEpic("PO-1286")
    @JiraTestKey("PO-7646")
    void getAtAGlance_notFoundReturns404() throws Exception {
        when(userStateService.getUserStateV1FromSecurityContext())
            .thenReturn(UserStateUtil.permissionUser((short) 77, SEARCH_AND_VIEW_ACCOUNTS));

        ResultActions actions = mockMvc.perform(get(URL, 999999L)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER));

        actions.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(header().doesNotExist(HttpHeaders.ETAG))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"))
            .andExpect(jsonPath("$.title").value("Entity Not Found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("The requested entity could not be found"))
            .andExpect(jsonPath("$.retriable").value(false));

        JsonNode problem = objectMapper.readTree(actions.andReturn().getResponse().getContentAsString());
        assertStandardProblemResponse(problem,
            404,
            "Entity Not Found",
            "The requested entity could not be found",
            "https://hmcts.gov.uk/problems/entity-not-found",
            false);
    }

    @Test
    @DisplayName("PO-2132 Opal missing auth returns 401")
    @JiraStory("PO-2132")
    @JiraEpic("PO-1286")
    @JiraTestKey("PO-7639")
    void getAtAGlance_missingAuthReturns401() throws Exception {
        doThrow(new ResponseStatusException(UNAUTHORIZED, "Unauthorized"))
            .when(userStateService).getUserStateV1FromSecurityContext();

        mockMvc.perform(get(URL, MJ_ACCOUNT_ID))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail").value("Unauthorized"))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @Test
    @DisplayName("PO-2132 Opal missing permission returns 403")
    @JiraStory("PO-2132")
    @JiraEpic("PO-1286")
    @JiraTestKey("PO-7640")
    void getAtAGlance_missingPermissionReturns403() throws Exception {
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(UserStateUtil.noPermissionsUser());

        mockMvc.perform(get(URL, MJ_ACCOUNT_ID).header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @Test
    @DisplayName("PO-2132 Opal query timeout returns 408 retriable problem response")
    @JiraStory("PO-2132")
    @JiraEpic("PO-1286")
    @JiraTestKey("PO-7648")
    void getAtAGlance_queryTimeoutReturns408() throws Exception {
        when(userStateService.getUserStateV1FromSecurityContext())
            .thenReturn(UserStateUtil.permissionUser((short) 77, SEARCH_AND_VIEW_ACCOUNTS));
        doThrow(new QueryTimeoutException("timeout", null, null))
            .when(userStateService).getUserStateV1FromSecurityContext();

        mockMvc.perform(get(URL, MJ_ACCOUNT_ID).header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
            .andExpect(status().isRequestTimeout())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.retriable").value(true));
    }

    @Test
    @DisplayName("PO-2132 Opal data access failure returns 503 retriable problem response")
    @JiraStory("PO-2132")
    @JiraEpic("PO-1286")
    @JiraTestKey("PO-7645")
    void getAtAGlance_dataAccessFailureReturns503() throws Exception {
        when(userStateService.getUserStateV1FromSecurityContext())
            .thenReturn(UserStateUtil.permissionUser((short) 77, SEARCH_AND_VIEW_ACCOUNTS));
        doThrow(new DataAccessResourceFailureException("db unavailable"))
            .when(majorCreditorAccountAtAGlanceRepository).findById(MJ_ACCOUNT_ID);

        mockMvc.perform(get(URL, MJ_ACCOUNT_ID).header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.retriable").value(true));
    }

    @Test
    @DisplayName("PO-2132 Opal internal server error returns 500 problem response")
    @JiraStory("PO-2132")
    @JiraEpic("PO-1286")
    @JiraTestKey("PO-7642")
    void getAtAGlance_internalServerErrorReturns500() throws Exception {
        when(userStateService.getUserStateV1FromSecurityContext())
            .thenReturn(UserStateUtil.permissionUser((short) 77, SEARCH_AND_VIEW_ACCOUNTS));
        doThrow(HttpServerErrorException.create(
            org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            HttpHeaders.EMPTY,
            null,
            null
        )).when(majorCreditorAccountAtAGlanceRepository).findById(anyLong());

        mockMvc.perform(get(URL, MJ_ACCOUNT_ID).header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    private Map<String, Object> getAtAGlanceRow(long creditorAccountId) {
        return jdbcTemplate.queryForMap("""
            SELECT v.creditor_account_id,
                   v.name,
                   v.address_line_1,
                   v.address_line_2,
                   v.address_line_3,
                   v.postcode,
                   ca.pay_by_bacs,
                   ca.version_number,
                   mc.major_creditor_code
              FROM v_major_creditor_account_at_a_glance v
              JOIN creditor_accounts ca
                ON ca.creditor_account_id = v.creditor_account_id
              LEFT JOIN major_creditors mc
                ON mc.major_creditor_id = ca.major_creditor_id
             WHERE v.creditor_account_id = ?
            """, creditorAccountId);
    }

    private void assertStandardProblemResponse(JsonNode problem,
                                               int statusCode,
                                               String title,
                                               String detail,
                                               String type,
                                               boolean retriable) {
        assertEquals(Set.of("type", "title", "status", "detail", "instance", "operation_id", "retriable", "reason"),
            fieldNames(problem));
        assertEquals(statusCode, problem.get("status").asInt());
        assertEquals(title, problem.get("title").asText());
        assertEquals(detail, problem.get("detail").asText());
        assertEquals(type, problem.get("type").asText());
        assertEquals(retriable, problem.get("retriable").asBoolean());
        assertTrue(problem.get("reason").asText().matches(".+"));
        assertTrue(problem.get("operation_id").asText().matches(".+"));
        assertTrue(problem.get("instance").asText().matches("https://hmcts.gov.uk/problems/instance/.+"));
    }

    private Set<String> fieldNames(JsonNode node) {
        Set<String> fields = new HashSet<>();
        fields.addAll(node.propertyNames());
        return fields;
    }

    @SuppressWarnings("unchecked")
    private void assertMatchesOpenApiSchema(JsonNode body) throws Exception {
        ClassPathResource resource = new ClassPathResource("openapi/MajorCreditor.yaml");
        Map<String, Object> root;
        try (InputStream inputStream = resource.getInputStream()) {
            root = new Yaml().load(inputStream);
        }

        Map<String, Object> paths = (Map<String, Object>) root.get("paths");
        Map<String, Object> endpoint = (Map<String, Object>) paths.get("/major-creditor-accounts/{id}/at-a-glance");
        Map<String, Object> get = (Map<String, Object>) endpoint.get("get");
        Map<String, Object> responses = (Map<String, Object>) get.get("responses");
        Map<String, Object> ok = (Map<String, Object>) responses.get("200");
        Map<String, Object> content = (Map<String, Object>) ok.get("content");
        Map<String, Object> applicationJson = (Map<String, Object>) content.get("application/json");
        Map<String, Object> schema = (Map<String, Object>) applicationJson.get("schema");

        assertMatchesSchema(body, schema);
    }

    @SuppressWarnings("unchecked")
    private void assertMatchesSchema(JsonNode node, Map<String, Object> schema) {
        assertEquals("object", schema.get("type"));

        Map<String, Object> schemaProperties =
            (Map<String, Object>) schema.getOrDefault("properties", Collections.emptyMap());
        Set<String> allowedProperties = schemaProperties.keySet();
        assertTrue(
            allowedProperties.containsAll(fieldNames(node)),
            "Unexpected properties present. Allowed: " + allowedProperties + ", actual: " + fieldNames(node)
        );

        for (String requiredField : (List<String>) schema.getOrDefault("required", List.of())) {
            assertTrue(node.has(requiredField), "Missing required field: " + requiredField);
        }

        for (String propertyName : fieldNames(node)) {
            Map<String, Object> propertySchema = (Map<String, Object>) schemaProperties.get(propertyName);
            JsonNode value = node.get(propertyName);
            if (value != null
                && value.isObject()
                && propertySchema != null
                && "object".equals(propertySchema.get("type"))) {
                assertMatchesSchema(value, propertySchema);
            }
        }
    }
}
