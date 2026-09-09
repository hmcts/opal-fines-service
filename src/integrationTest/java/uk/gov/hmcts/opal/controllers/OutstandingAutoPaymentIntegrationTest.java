package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;
import tools.jackson.databind.JsonNode;

@ActiveProfiles({"integration"})
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1c-payment=true"
})
@DisplayName("Outstanding Auto Payment Integration Tests")
@Sql(scripts = "classpath:db/insertData/insert_into_outstanding_auto_payment_count.sql",
     executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_outstanding_auto_payment_count.sql",
     executionPhase = AFTER_TEST_METHOD)
class OutstandingAutoPaymentIntegrationTest extends AbstractIntegrationTest {

    private static final String URL = "/business-units/outstanding-auto-payment-count";
    private static final List<Short> LUTON_AND_CARDIFF = List.of((short) 2470, (short) 2471);
    private static final List<Short> LUTON_ONLY = List.of((short) 2470);

    @MockitoBean
    private UserStateService userStateService;

    @Test
    @DisplayName("PO-2470 INT.01 - Returns outstanding counts")
    @JiraStory("PO-2470")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-2470")
    void returnsOutstandingCounts() throws Exception {
        when(userStateService.getBusinessUnitIdsFor(
            FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(LUTON_AND_CARDIFF);

        mockMvc.perform(get(URL))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.business_units", hasSize(2)))
            .andExpect(jsonPath("$.business_units[*].business_unit_name", contains("Cardiff", "Luton")))
            .andExpect(jsonPath("$.business_units[0].business_unit_id").value(2471))
            .andExpect(jsonPath("$.business_units[0].file_count").value(1))
            .andExpect(jsonPath("$.business_units[0].till_count").value(2))
            .andExpect(jsonPath("$.business_units[1].business_unit_id").value(2470))
            .andExpect(jsonPath("$.business_units[1].file_count").value(2))
            .andExpect(jsonPath("$.business_units[1].till_count").value(1));
    }

    @Test
    @DisplayName("PO-2470 INT.02 - Filters by permission and excludes unpermitted outstanding items")
    @JiraStory("PO-2470")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-2470")
    void filtersByPermission() throws Exception {
        when(userStateService.getBusinessUnitIdsFor(
            FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(LUTON_ONLY);

        mockMvc.perform(get(URL))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.business_units", hasSize(1)))
            .andExpect(jsonPath("$.business_units[0].business_unit_id").value(2470))
            .andExpect(jsonPath("$.business_units[0].business_unit_name").value("Luton"))
            .andExpect(jsonPath("$.business_units[0].file_count").value(2))
            .andExpect(jsonPath("$.business_units[0].till_count").value(1));
    }

    @Test
    @DisplayName("PO-2470 INT.03 - Returns 200 with empty response when user has no permission")
    @JiraStory("PO-2470")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-2470")
    void returnsEmptyResponseWhenUserHasNoPermission() throws Exception {
        when(userStateService.getBusinessUnitIdsFor(
            FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(List.of());

        mockMvc.perform(get(URL))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.business_units", hasSize(0)));
    }

    @Test
    @DisplayName("PO-2470 INT.04 - Repeated GET returns identical response")
    @JiraStory("PO-2470")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-2470")
    void repeatedGetReturnsIdenticalResponse() throws Exception {
        when(userStateService.getBusinessUnitIdsFor(
            FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(LUTON_AND_CARDIFF);

        String firstResponse = performSuccessfulRequest();
        String secondResponse = performSuccessfulRequest();

        assertEquals(firstResponse, secondResponse);
    }

    @Test
    @DisplayName("PO-2470 INT.05 - Returns only documented fields")
    @JiraStory("PO-2470")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-2470")
    void returnsOnlyDocumentedFields() throws Exception {
        when(userStateService.getBusinessUnitIdsFor(
            FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(LUTON_ONLY);

        MvcResult result = mockMvc.perform(get(URL))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode businessUnit = response.get("business_units").get(0);

        assertEquals(Set.of("business_units"), fieldNames(response));
        assertEquals(
            Set.of("business_unit_id", "business_unit_name", "file_count", "till_count"),
            fieldNames(businessUnit));
    }

    private String performSuccessfulRequest() throws Exception {
        return mockMvc.perform(get(URL))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.business_units[*].business_unit_name", contains("Cardiff", "Luton")))
            .andReturn()
            .getResponse()
            .getContentAsString();
    }

    private Set<String> fieldNames(JsonNode jsonNode) {
        return jsonNode.properties().stream()
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }
}
