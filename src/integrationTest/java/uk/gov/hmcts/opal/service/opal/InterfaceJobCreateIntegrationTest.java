package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateItem;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateRequest;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateResponse;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsFileSource;
import uk.gov.hmcts.opal.repository.InterfaceFileRepository;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration"})
@DisplayName("Interface Job Create Integration Tests")
@Sql(scripts = "classpath:db/insertData/insert_into_interface_jobs_create.sql",
     executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_interface_jobs_create.sql",
     executionPhase = AFTER_TEST_METHOD)
class InterfaceJobCreateIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private InterfaceJobService interfaceJobService;

    @Autowired
    private InterfaceJobRepository interfaceJobRepository;

    @Autowired
    private InterfaceFileRepository interfaceFileRepository;

    @Test
    @JiraStory("PO-2577")
    @JiraEpic("PO-304")
    void shouldCreateInterfaceJobsAndFiles() {
        InterfaceJobsCreateResponse response = interfaceJobService.create(
            InterfaceJobsCreateRequest.builder()
                .interfaceJobs(List.of(createJob((short) 2577, "Auto Payments In Create")))
                .build());

        assertEquals(1, response.getInterfaceJobs().size());
        Long interfaceJobId = response.getInterfaceJobs().getFirst().getInterfaceJobId();

        assertNotNull(interfaceJobId);
        InterfaceJobEntity interfaceJob = interfaceJobRepository.findById(interfaceJobId).orElseThrow();
        InterfaceFileEntity interfaceFile = fileFor(interfaceJobId);

        assertEquals(InterfaceJobStatus.CREATED, interfaceJob.getStatus());
        assertEquals("Auto Payments In Create", interfaceJob.getInterfaceName());
        assertEquals("auto-payments-in-create.dat", interfaceFile.getFileName());
        assertEquals("NATWEST", interfaceFile.getSource());
        assertTrue(interfaceFile.getRecords().contains("abc123"));
    }

    @Test
    @JiraStory("PO-2577")
    @JiraEpic("PO-304")
    void shouldRollbackWhenOneInterfaceJobCannotBeCreated() {
        InterfaceJobsCreateRequest request = InterfaceJobsCreateRequest.builder()
            .interfaceJobs(List.of(
                createJob((short) 2577, "Auto Payments In Rollback"),
                createJob((short) 9999, "Auto Payments In Rollback")))
            .build();

        assertThrows(EntityNotFoundException.class, () -> interfaceJobService.create(request));

        assertEquals(0, jobsByInterfaceName("Auto Payments In Rollback").size());
    }

    private InterfaceJobsCreateItem createJob(Short businessUnitId, String interfaceName) {
        return InterfaceJobsCreateItem.builder()
            .fileName(interfaceName.toLowerCase().replace(" ", "-") + ".dat")
            .source(InterfaceJobsFileSource.NATWEST)
            .records("[{\"account\":\"abc123\"}]")
            .businessUnitId(businessUnitId)
            .interfaceName(interfaceName)
            .createdDatetime(LocalDateTime.of(2026, 7, 14, 10, 0))
            .build();
    }

    private List<InterfaceJobEntity> jobsByInterfaceName(String interfaceName) {
        return interfaceJobRepository.findAll().stream()
            .filter(interfaceJob -> interfaceName.equals(interfaceJob.getInterfaceName()))
            .toList();
    }

    private InterfaceFileEntity fileFor(Long interfaceJobId) {
        return interfaceFileRepository.findAll().stream()
            .filter(interfaceFile -> interfaceJobId.equals(interfaceFile.getInterfaceJob().getInterfaceJobId()))
            .findFirst()
            .orElseThrow();
    }
}
