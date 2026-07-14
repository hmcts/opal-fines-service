package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.repository.InterfaceFileRepository;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

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

    @Autowired
    private InterfaceJobRepository interfaceJobRepository;

    @Autowired
    private InterfaceFileRepository interfaceFileRepository;

    @Test
    @DisplayName("PO-2577 INT.04 - Returns documented response")
    @JiraStory("PO-2577")
    @JiraEpic("PO-304")
    void returnsDocumentedResponse() throws Exception {
        MvcResult result = mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody("auto-payments-in-endpoint.dat", "Auto Payments In Endpoint")))
            .andExpect(status().isCreated())
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
    @DisplayName("PO-2577 INT.06 - Persists mapped job and file")
    @JiraStory("PO-2577")
    @JiraEpic("PO-304")
    void persistsMappedJobAndFile() throws Exception {
        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody("auto-payments-in-endpoint.dat", "Auto Payments In Endpoint")))
            .andExpect(status().isCreated());

        InterfaceJobEntity interfaceJob = singleJob("Auto Payments In Endpoint");
        InterfaceFileEntity interfaceFile = singleFile(interfaceJob.getInterfaceJobId());

        assertEquals(InterfaceJobStatus.CREATED, interfaceJob.getStatus());
        assertEquals("auto-payments-in-endpoint.dat", interfaceFile.getFileName());
    }

    @Test
    @DisplayName("PO-2577 INT.08 - Creates duplicate payloads")
    @JiraStory("PO-2577")
    @JiraEpic("PO-304")
    void createsDuplicatePayloads() throws Exception {
        String requestBody = requestBody("auto-payments-in-duplicate.dat", "Auto Payments In Duplicate");

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated());
        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated());

        assertEquals(2, jobsByInterfaceName("Auto Payments In Duplicate").size());
        assertEquals(2, filesFor(jobsByInterfaceName("Auto Payments In Duplicate")).size());
    }

    @Test
    @DisplayName("PO-2577 INT.04 - Rejects invalid request")
    @JiraStory("PO-2577")
    @JiraEpic("PO-304")
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

    private String requestBody(String fileName, String interfaceName) {
        return """
               {
                 "interface_jobs": [
                   {
                     "file_name": "%s",
                     "source": "NATWEST",
                     "records": "[{\\"account\\":\\"abc123\\"}]",
                     "business_unit_id": 2577,
                     "interface_name": "%s",
                     "created_datetime": "2026-07-14T10:00:00"
                   }
                 ]
               }
               """.formatted(fileName, interfaceName);
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
