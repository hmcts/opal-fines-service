package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateItem;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateRequest;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateResponse;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsFileSource;
import uk.gov.hmcts.opal.repository.InterfaceFileRepository;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration"})
@DisplayName("Interface Job Create Integration Tests")
@Sql(scripts = "classpath:db/insertData/insert_into_interface_jobs_create.sql",
     executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_interface_jobs_create.sql",
     executionPhase = AFTER_TEST_METHOD)
class InterfaceJobCreateIntegrationTest extends AbstractIntegrationTest {

    private static final Short BUSINESS_UNIT_ID = 2577;
    private static final Short MISSING_BUSINESS_UNIT_ID = 9999;

    @Autowired
    private InterfaceJobService interfaceJobService;

    @Autowired
    private InterfaceJobRepository interfaceJobRepository;

    @Autowired
    private InterfaceFileRepository interfaceFileRepository;

    @MockitoBean
    private UserStateService userStateService;

    @Test
    @JiraStory("PO-2577")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-10187")
    void shouldCreateInterfaceJobsAndFiles() {
        stubPermission(BUSINESS_UNIT_ID);

        InterfaceJobsCreateResponse response = interfaceJobService.create(
            InterfaceJobsCreateRequest.builder()
                .interfaceJobs(List.of(createJob(BUSINESS_UNIT_ID, "Auto Payments In Create")))
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
        assertEquals(objectMapper.readTree(paymentRecords()), objectMapper.readTree(interfaceFile.getRecords()));
        assertEquals((short) 1, interfaceFile.getRecordCount());
        assertEquals(new BigDecimal("123.45"), interfaceFile.getTotalAmount());
    }

    @Test
    @DisplayName("Completed file summary view exposes the supplied numeric totals")
    @JiraStory("PO-10680")
    @JiraEpic("PO-2468")
    void shouldExposeTotalsInProcessedFileSummary() {
        stubPermission(BUSINESS_UNIT_ID);
        InterfaceJobsCreateResponse response = interfaceJobService.create(
            InterfaceJobsCreateRequest.builder()
                .interfaceJobs(List.of(createJob(BUSINESS_UNIT_ID, "Auto Payments In Create")))
                .build());
        Long interfaceJobId = response.getInterfaceJobs().getFirst().getInterfaceJobId();
        InterfaceJobEntity interfaceJob = interfaceJobRepository.findById(interfaceJobId).orElseThrow();
        interfaceJob.setStatus(InterfaceJobStatus.COMPLETED);
        interfaceJobRepository.save(interfaceJob);

        Map<String, Object> summary = jdbcTemplate.queryForMap("""
            SELECT total_records, total_amount
            FROM v_interface_jobs_processed_file_summary
            WHERE interface_job_id = ?
            """, interfaceJobId);

        assertEquals(1, summary.get("total_records"));
        assertEquals(new BigDecimal("123.45"), summary.get("total_amount"));
    }

    @Test
    @JiraStory("PO-2577")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-10188")
    void shouldRollbackWhenOneInterfaceJobCannotBeCreated() {
        stubPermission(BUSINESS_UNIT_ID, MISSING_BUSINESS_UNIT_ID);

        InterfaceJobsCreateRequest request = InterfaceJobsCreateRequest.builder()
            .interfaceJobs(List.of(
                createJob(BUSINESS_UNIT_ID, "Auto Payments In Rollback"),
                createJob(MISSING_BUSINESS_UNIT_ID, "Auto Payments In Rollback")))
            .build();

        assertThrows(EntityNotFoundException.class, () -> interfaceJobService.create(request));

        assertEquals(0, jobsByInterfaceName("Auto Payments In Rollback").size());
    }

    private InterfaceJobsCreateItem createJob(Short businessUnitId, String interfaceName) {
        return InterfaceJobsCreateItem.builder()
            .fileName(interfaceName.toLowerCase().replace(" ", "-") + ".dat")
            .source(InterfaceJobsFileSource.NATWEST)
            .records(paymentRecords())
            .recordCount((short) 1)
            .totalAmount(new BigDecimal("123.45"))
            .businessUnitId(businessUnitId)
            .interfaceName(interfaceName)
            .createdDatetime(LocalDateTime.of(2026, 7, 14, 10, 0))
            .build();
    }

    private void stubPermission(Short... businessUnitIds) {
        for (Short businessUnitId : businessUnitIds) {
            List<Short> businessUnitIdList = List.of(businessUnitId);
            when(userStateService.getPermittedBusinessUnitIds(
                businessUnitIdList, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(businessUnitIdList);
        }
    }

    private String paymentRecords() {
        return """
            [{
              "receiving_sort_code": "123456",
              "receiving_bank_account_number": "01234567",
              "receiving_account_type": "5",
              "transaction_code": "68",
              "originator_sort_code": "654321",
              "originator_bank_account_number": "98765432",
              "amount_pence": "12345",
              "originator_name": "Test Payer",
              "originator_reference": "99000001A",
              "originator_beneficiary_name": "Test Court"
            }]
            """;
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
