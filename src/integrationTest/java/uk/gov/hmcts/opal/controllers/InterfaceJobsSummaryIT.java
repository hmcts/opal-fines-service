package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
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
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration"})
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1c-payment=true"
})
@DisplayName("Interface Jobs Summary Integration Tests")
@Sql(scripts = "classpath:db/insertData/insert_into_interface_jobs_summary.sql",
     executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_interface_jobs_summary.sql",
     executionPhase = AFTER_TEST_METHOD)
class InterfaceJobsSummaryIT extends AbstractIntegrationTest {

    private static final String URL = "/interface-jobs/summary";
    private static final List<Short> LUTON_AND_CARDIFF = List.of((short) 2574, (short) 2575);
    private static final List<Short> LUTON_ONLY = List.of((short) 2574);
    private static final List<Short> CARDIFF_ONLY = List.of((short) 2575);

    @MockitoBean
    private UserStateService userStateService;

    @Test
    @DisplayName("PO-2574 INT.01 - Returns permitted summary")
    @JiraStory("PO-2574")
    @JiraEpic("PO-304")
    void returnsSummary() throws Exception {
        when(userStateService.getPermittedBusinessUnitIds(
            LUTON_AND_CARDIFF, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(LUTON_AND_CARDIFF);

        mockMvc.perform(get(URL)
                .queryParam("business_unit_ids", "2574", "2575")
                .queryParam("statuses", "COMPLETED")
                .queryParam("completed_date_from", "2026-07-01T10:00:00")
                .queryParam("completed_date_to", "2026-07-01T12:00:00")
                .queryParam("interface_name", "Auto Payments In"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.interface_jobs", hasSize(3)))
            .andExpect(jsonPath("$.interface_jobs[*].business_unit_name",
                containsInAnyOrder("Luton", "Luton", "Cardiff")))
            .andExpect(jsonPath("$.interface_jobs[*].file_name",
                containsInAnyOrder("auto-payments-in-1.dat", "auto-payments-in-2.dat",
                    "cardiff-auto-payments-in.dat")));
    }

    @Test
    @DisplayName("PO-2574 INT.02 - Filters by permission")
    @JiraStory("PO-2574")
    @JiraEpic("PO-304")
    void filtersByPermission() throws Exception {
        when(userStateService.getPermittedBusinessUnitIds(
            LUTON_AND_CARDIFF, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(LUTON_ONLY);

        mockMvc.perform(get(URL)
                .queryParam("business_unit_ids", "2574", "2575")
                .queryParam("statuses", "COMPLETED")
                .queryParam("interface_name", "Auto Payments In"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.interface_jobs", hasSize(2)))
            .andExpect(jsonPath("$.interface_jobs[*].business_unit_name", containsInAnyOrder("Luton", "Luton")))
            .andExpect(jsonPath("$.interface_jobs[*].file_name",
                containsInAnyOrder("auto-payments-in-1.dat", "auto-payments-in-2.dat")));
    }

    @Test
    @DisplayName("PO-2574 INT.03 - Filters by status")
    @JiraStory("PO-2574")
    @JiraEpic("PO-304")
    void filtersByStatus() throws Exception {
        when(userStateService.getPermittedBusinessUnitIds(
            LUTON_AND_CARDIFF, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(LUTON_AND_CARDIFF);

        mockMvc.perform(get(URL)
                .queryParam("business_unit_ids", "2574", "2575")
                .queryParam("statuses", "FAILED")
                .queryParam("interface_name", "Auto Payments In"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.interface_jobs", hasSize(1)))
            .andExpect(jsonPath("$.interface_jobs[0].interface_job_id").value(257403))
            .andExpect(jsonPath("$.interface_jobs[0].status").value("FAILED"));
    }

    @Test
    @DisplayName("PO-2574 INT.04 - Filters by completed date")
    @JiraStory("PO-2574")
    @JiraEpic("PO-304")
    void filtersByCompletedDate() throws Exception {
        when(userStateService.getPermittedBusinessUnitIds(
            LUTON_AND_CARDIFF, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(LUTON_AND_CARDIFF);

        mockMvc.perform(get(URL)
                .queryParam("business_unit_ids", "2574", "2575")
                .queryParam("completed_date_from", "2026-07-01T11:30:00")
                .queryParam("completed_date_to", "2026-07-01T11:30:00"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.interface_jobs", hasSize(1)))
            .andExpect(jsonPath("$.interface_jobs[0].interface_job_id").value(257402))
            .andExpect(jsonPath("$.interface_jobs[0].file_name").value("cardiff-auto-payments-in.dat"));
    }

    @Test
    @DisplayName("PO-2574 INT.05 - Filters by interface and BU")
    @JiraStory("PO-2574")
    @JiraEpic("PO-304")
    void filtersByInterfaceAndBu() throws Exception {
        when(userStateService.getPermittedBusinessUnitIds(
            CARDIFF_ONLY, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(CARDIFF_ONLY);

        mockMvc.perform(get(URL)
                .queryParam("business_unit_ids", "2575")
                .queryParam("interface_name", "Manual Payments In"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.interface_jobs", hasSize(1)))
            .andExpect(jsonPath("$.interface_jobs[0].interface_job_id").value(257404))
            .andExpect(jsonPath("$.interface_jobs[0].file_name").value("manual-payments-in.dat"))
            .andExpect(jsonPath("$.interface_jobs[0].business_unit_name").value("Cardiff"));
    }

    @Test
    @DisplayName("PO-2574 INT.06 - Returns joined data")
    @JiraStory("PO-2574")
    @JiraEpic("PO-304")
    void returnsJoinedData() throws Exception {
        when(userStateService.getPermittedBusinessUnitIds(
            LUTON_ONLY, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(LUTON_ONLY);

        mockMvc.perform(get(URL)
                .queryParam("business_unit_ids", "2574")
                .queryParam("statuses", "FAILED"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.interface_jobs", hasSize(1)))
            .andExpect(jsonPath("$.interface_jobs[0].interface_job_id").value(257403))
            .andExpect(jsonPath("$.interface_jobs[0].interface_file_id").value(257414))
            .andExpect(jsonPath("$.interface_jobs[0].file_name").value("failed-auto-payments-in.dat"))
            .andExpect(jsonPath("$.interface_jobs[0].source").value("DWP"))
            .andExpect(jsonPath("$.interface_jobs[0].business_unit_name").value("Luton"))
            .andExpect(jsonPath("$.interface_jobs[0].status").value("FAILED"));
    }

    @Test
    @DisplayName("PO-2574 INT.07 - Returns documented fields")
    @JiraStory("PO-2574")
    @JiraEpic("PO-304")
    void returnsDocumentedFields() throws Exception {
        when(userStateService.getPermittedBusinessUnitIds(
            LUTON_ONLY, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(LUTON_ONLY);

        MvcResult result = mockMvc.perform(get(URL)
                .queryParam("business_unit_ids", "2574")
                .queryParam("statuses", "FAILED")
                .queryParam("interface_name", "Auto Payments In"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode item = response.get("interface_jobs").get(0);

        assertEquals(Set.of("interface_jobs"), response.properties().stream()
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet()));
        assertEquals(
            Set.of("interface_job_id", "interface_file_id", "file_name", "source", "business_unit_name",
                   "completed_datetime", "created_datetime", "status"),
            item.properties().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet()));
    }

    @Test
    @DisplayName("PO-2574 INT.08 - Rejects invalid parameters")
    @JiraStory("PO-2574")
    @JiraEpic("PO-304")
    void rejectsInvalidStatus() throws Exception {
        when(userStateService.getPermittedBusinessUnitIds(
            LUTON_ONLY, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(LUTON_ONLY);

        mockMvc.perform(get(URL)
                .queryParam("business_unit_ids", "2574")
                .queryParam("statuses", "NOT_A_STATUS")
                .queryParam("interface_name", "Auto Payments In"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("PO-2574 INT.09 - Uses row-level permissions")
    @JiraStory("PO-2574")
    @JiraEpic("PO-304")
    void usesRowLevelPermissions() throws Exception {
        when(userStateService.getPermittedBusinessUnitIds(
            LUTON_AND_CARDIFF, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(List.of());

        mockMvc.perform(get(URL)
                .queryParam("business_unit_ids", "2574", "2575")
                .queryParam("statuses", "COMPLETED")
                .queryParam("interface_name", "Auto Payments In"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.interface_jobs", hasSize(0)));

        verify(userStateService).getPermittedBusinessUnitIds(
            LUTON_AND_CARDIFF, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);
    }

    @Test
    @DisplayName("PO-2574 INT.10 - Returns deterministic response")
    @JiraStory("PO-2574")
    @JiraEpic("PO-304")
    void returnsDeterministicResponse() throws Exception {
        when(userStateService.getPermittedBusinessUnitIds(
            LUTON_ONLY, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(LUTON_ONLY);

        MvcResult firstResult = mockMvc.perform(get(URL)
                .queryParam("business_unit_ids", "2574")
                .queryParam("statuses", "FAILED")
                .queryParam("interface_name", "Auto Payments In"))
            .andExpect(status().isOk())
            .andReturn();

        MvcResult secondResult = mockMvc.perform(get(URL)
                .queryParam("business_unit_ids", "2574")
                .queryParam("statuses", "FAILED")
                .queryParam("interface_name", "Auto Payments In"))
            .andExpect(status().isOk())
            .andReturn();

        assertEquals(firstResult.getResponse().getContentAsString(), secondResult.getResponse().getContentAsString());
    }
}
