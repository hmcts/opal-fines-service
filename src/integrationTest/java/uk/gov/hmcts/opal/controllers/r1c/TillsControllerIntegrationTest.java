package uk.gov.hmcts.opal.controllers.r1c;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration"})
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1c-payment=true",
    "launchdarkly.default-flag-values.is-legacy-mode=false",
    "launchdarkly.enabled=false"
})
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql(scripts = "classpath:db/insertData/insert_into_tills_summary.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_tills_summary.sql", executionPhase = AFTER_TEST_METHOD)
@DisplayName("Tills Controller Integration Test")
class TillsControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/tills";

    @BeforeEach
    void setUp() {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 25750, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);
    }

    @Test
    @DisplayName("PO-2575 INT.01 - Returns permitted tills from v_till_summary")
    @JiraStory("PO-2575")
    @JiraEpic("PO-2532")
    void getTills_returnsPermittedTills() throws Exception {
        mockMvc.perform(get(URL_BASE)
                .param("business_unit_ids", "25750", "25751")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.tills", hasSize(2)))
            .andExpect(jsonPath("$.tills[*].till_number", containsInAnyOrder(501, 502)))
            .andExpect(jsonPath("$.tills[*].business_unit_name", containsInAnyOrder("Luton", "Luton")))
            .andExpect(jsonPath("$.tills[*].processed_by", containsInAnyOrder("L25750", "L25750")));
    }

    @Test
    @DisplayName("PO-2575 INT.02 - Filters by status and auto payments")
    @JiraStory("PO-2575")
    @JiraEpic("PO-2532")
    void getTills_filtersByStatusAndAutoPayments() throws Exception {
        mockMvc.perform(get(URL_BASE)
                .param("business_unit_ids", "25750")
                .param("statuses", "Allocated")
                .param("auto_payments", "true")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tills", hasSize(1)))
            .andExpect(jsonPath("$.tills[0].till_number").value(501))
            .andExpect(jsonPath("$.tills[0].errors").value(2))
            .andExpect(jsonPath("$.tills[0].file_name").value("luton-allocated.dat"))
            .andExpect(jsonPath("$.tills[0].source").value("NATWEST"))
            .andExpect(jsonPath("$.tills[0].amount").value(1234.56))
            .andExpect(jsonPath("$.tills[0].business_unit_name").value("Luton"))
            .andExpect(jsonPath("$.tills[0].processed_by").value("L25750"))
            .andExpect(jsonPath("$.tills[0].date_processed").value("2026-08-27"));
    }

    @Test
    @DisplayName("PO-2575 INT.03 - Filters by non-auto payments")
    @JiraStory("PO-2575")
    @JiraEpic("PO-2532")
    void getTills_filtersByNonAutoPayments() throws Exception {
        mockMvc.perform(get(URL_BASE)
                .param("business_unit_ids", "25750")
                .param("auto_payments", "false")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tills", hasSize(1)))
            .andExpect(jsonPath("$.tills[0].till_number").value(502))
            .andExpect(jsonPath("$.tills[0].errors").value(1))
            .andExpect(jsonPath("$.tills[0].file_name").value("luton-created.dat"))
            .andExpect(jsonPath("$.tills[0].source").value("ALLPAY"))
            .andExpect(jsonPath("$.tills[0].amount").value(2345.67));
    }

    @Test
    @DisplayName("PO-2575 INT.04 - Returns empty array when no requested BU is permitted")
    @JiraStory("PO-2575")
    @JiraEpic("PO-2532")
    void getTills_returnsEmptyArrayWhenNoRequestedBusinessUnitIsPermitted() throws Exception {
        mockMvc.perform(get(URL_BASE)
                .param("business_unit_ids", "25751")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tills").isArray())
            .andExpect(jsonPath("$.tills").isEmpty());
    }

    @Test
    @DisplayName("PO-2575 INT.05 - Returns only documented fields")
    @JiraStory("PO-2575")
    @JiraEpic("PO-2532")
    void getTills_returnsOnlyDocumentedFields() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE)
            .param("business_unit_ids", "25750")
            .param("statuses", "Allocated")
            .with(userStateStub.getAuthenticaitonRequestPostProcessor()));

        String body = actions.andReturn().getResponse().getContentAsString();
        Set<String> actualFields = objectMapper.readTree(body).get("tills").get(0).properties()
            .stream()
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());

        actions.andExpect(status().isOk());
        assertEquals(Set.of(
            "till_number",
            "errors",
            "file_name",
            "source",
            "amount",
            "business_unit_name",
            "processed_by",
            "date_processed"
        ), actualFields, ToJsonString.toPrettyJson(body));
    }

    @Test
    @DisplayName("PO-2575 INT.06 - GET is deterministic for repeated identical request")
    @JiraStory("PO-2575")
    @JiraEpic("PO-2532")
    void getTills_returnsIdenticalOutputWhenRepeated() throws Exception {
        ResultActions first = mockMvc.perform(get(URL_BASE)
            .param("business_unit_ids", "25750")
            .with(userStateStub.getAuthenticaitonRequestPostProcessor()));
        ResultActions second = mockMvc.perform(get(URL_BASE)
            .param("business_unit_ids", "25750")
            .with(userStateStub.getAuthenticaitonRequestPostProcessor()));

        String firstBody = first.andReturn().getResponse().getContentAsString();
        String secondBody = second.andReturn().getResponse().getContentAsString();

        first.andExpect(status().isOk());
        second.andExpect(status().isOk());
        assertEquals(objectMapper.readTree(firstBody), objectMapper.readTree(secondBody),
            ToJsonString.toPrettyJson(secondBody));
    }

    @Test
    @DisplayName("PO-2575 INT.07 - Missing token returns forbidden")
    @JiraStory("PO-2575")
    @JiraEpic("PO-2532")
    void getTills_whenNoTokenPresent_returnsForbidden() throws Exception {
        mockMvc.perform(get(URL_BASE))
            .andExpect(status().isForbidden());
    }
}
