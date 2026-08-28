package uk.gov.hmcts.opal.controllers.shared;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.repository.InterfaceFileRepository;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration"})
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1c-payment=true"
})
@DisplayName("Interface Jobs Create Integration Tests")
@Sql(scripts = "classpath:db/insertData/insert_into_interface_jobs_create.sql",
     executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_interface_jobs_create.sql",
     executionPhase = AFTER_TEST_METHOD)
class InterfaceJobsCreateIT extends AbstractIntegrationTest {

    private static final String URL = "/interface-jobs";
    private static final Short BUSINESS_UNIT_ID = 2577;
    private static final String INTERFACE_NAME = "Auto Payments In Endpoint";
    private static final String ROLLBACK_INTERFACE_NAME = "Auto Payments In Rollback";
    private static final String DUPLICATE_INTERFACE_NAME = "Auto Payments In Duplicate";

    @Autowired
    private InterfaceJobRepository interfaceJobRepository;

    @Autowired
    private InterfaceFileRepository interfaceFileRepository;

    @MockitoBean
    private UserStateService userStateService;

    @Test
    @DisplayName("PO-2577 INT.04/05 - Returns documented response")
    @JiraStory("PO-2577")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-10080")
    void returnsDocumentedResponse() throws Exception {
        stubPermission();

        MvcResult result = mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody("auto-payments-in-endpoint.dat", INTERFACE_NAME)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.interface_jobs", hasSize(1)))
            .andExpect(jsonPath("$.interface_jobs[0].interface_job_id").isNumber())
            .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode item = response.get("interface_jobs").get(0);

        assertEquals(Set.of("interface_jobs"), response.properties().stream()
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet()));
        assertEquals(Set.of("interface_job_id"), item.properties().stream()
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet()));
    }

    @Test
    @DisplayName("PO-2577 INT.01/02/06 - Persists mapped job and file")
    @JiraStory("PO-2577")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-10082")
    void persistsMappedJobAndFile() throws Exception {
        stubPermission();

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody("auto-payments-in-endpoint.dat", INTERFACE_NAME)))
            .andExpect(status().isOk());

        InterfaceJobEntity interfaceJob = singleJob(INTERFACE_NAME);
        InterfaceFileEntity interfaceFile = singleFile(interfaceJob.getInterfaceJobId());

        assertEquals(InterfaceJobStatus.CREATED, interfaceJob.getStatus());
        assertEquals(INTERFACE_NAME, interfaceJob.getInterfaceName());
        assertEquals("auto-payments-in-endpoint.dat", interfaceFile.getFileName());
        assertEquals("NATWEST", interfaceFile.getSource());
        assertEquals(
            objectMapper.readTree("[{\"account\":\"abc123\"}]"),
            objectMapper.readTree(interfaceFile.getRecords()));
    }

    @Test
    @DisplayName("PO-2577 INT.08 - Creates duplicate payloads")
    @JiraStory("PO-2577")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-10079")
    void createsDuplicatePayloads() throws Exception {
        stubPermission();
        String requestBody = requestBody("auto-payments-in-duplicate.dat", DUPLICATE_INTERFACE_NAME);

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk());
        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk());

        assertEquals(2, jobsByInterfaceName(DUPLICATE_INTERFACE_NAME).size());
        assertEquals(2, filesFor(jobsByInterfaceName(DUPLICATE_INTERFACE_NAME)).size());
    }

    @Test
    @DisplayName("PO-2577 INT.04 - Rejects invalid request")
    @JiraStory("PO-2577")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-10083")
    void rejectsInvalidRequest() throws Exception {
        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("""
                         {
                           "interface_jobs": []
                         }
                         """))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PO-2577 INT.03 - Rejects create without permission")
    @JiraStory("PO-2577")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-10081")
    void rejectsCreateWithoutPermission() throws Exception {
        when(userStateService.getPermittedBusinessUnitIds(
            List.of(BUSINESS_UNIT_ID), FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS))
            .thenReturn(List.of());

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody("auto-payments-in-forbidden.dat", "Forbidden Interface Jobs")))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.detail").value("You do not have permission to access this resource"))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    private String requestBody(String fileName, String interfaceName) {
        return """
               {
                 "interface_jobs": [
                   {
                     "file_name": "%s",
                     "source": "NATWEST",
                     "records": "[{\\"account\\":\\"abc123\\"}]",
                     "business_unit_id": %d,
                     "interface_name": "%s",
                     "created_datetime": "2026-07-14T10:00:00"
                   }
                 ]
               }
               """.formatted(fileName, BUSINESS_UNIT_ID, interfaceName);
    }

    private void stubPermission() {
        when(userStateService.getPermittedBusinessUnitIds(
            List.of(BUSINESS_UNIT_ID), FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS))
            .thenReturn(List.of(BUSINESS_UNIT_ID));
    }

    private InterfaceJobEntity singleJob(String interfaceName) {
        return jobsByInterfaceName(interfaceName).getFirst();
    }

    private InterfaceFileEntity singleFile(Long interfaceJobId) {
        return interfaceFileRepository.findAll().stream()
            .filter(interfaceFile -> interfaceJobId.equals(interfaceFile.getInterfaceJob().getInterfaceJobId()))
            .findFirst()
            .orElseThrow();
    }

    private List<InterfaceJobEntity> jobsByInterfaceName(String interfaceName) {
        return interfaceJobRepository.findAll().stream()
            .filter(interfaceJob -> interfaceName.equals(interfaceJob.getInterfaceName()))
            .toList();
    }

    private List<InterfaceFileEntity> filesFor(List<InterfaceJobEntity> interfaceJobs) {
        Set<Long> interfaceJobIds = interfaceJobs.stream()
            .map(InterfaceJobEntity::getInterfaceJobId)
            .collect(Collectors.toSet());

        return interfaceFileRepository.findAll().stream()
            .filter(interfaceFile -> interfaceJobIds.contains(interfaceFile.getInterfaceJob().getInterfaceJobId()))
            .toList();
    }
}
