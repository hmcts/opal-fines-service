package uk.gov.hmcts.opal.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity_;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity_;
import uk.gov.hmcts.opal.repository.jpa.InterfaceJobSpecs;
import uk.gov.hmcts.opal.service.opal.InterfaceJobService.InterfaceJobSearchCriteria;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration"})
@DisplayName("Interface Job Repository Integration Tests")
@Sql(scripts = "classpath:db/insertData/insert_into_interface_jobs_summary.sql",
     executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_interface_jobs_summary.sql",
     executionPhase = AFTER_TEST_METHOD)
class InterfaceJobRepositoryIntegrationTest extends AbstractIntegrationTest {

    private final InterfaceJobSpecs specs = new InterfaceJobSpecs();

    @Autowired
    private InterfaceJobRepository interfaceJobRepository;

    @Test
    @JiraStory("PO-2574")
    @JiraEpic("PO-304")
    void shouldFilterInterfaceJobSummaryBySearchCriteria() {
        InterfaceJobSearchCriteria searchCriteria = InterfaceJobSearchCriteria.builder()
            .permittedBusinessUnitIds(List.of((short) 2574))
            .statuses(List.of("COMPLETED"))
            .completedDateFrom(LocalDateTime.of(2026, 7, 1, 10, 0))
            .completedDateTo(LocalDateTime.of(2026, 7, 1, 11, 0))
            .interfaceName("auto payments in")
            .build();

        Page<InterfaceJobEntity> page = interfaceJobRepository.findBy(
            specs.findBySearchCriteria(searchCriteria),
            ffq -> ffq.sortBy(Sort.by(
                    Sort.Order.asc(InterfaceJobEntity_.BUSINESS_UNIT + "." + BusinessUnitEntity_.BUSINESS_UNIT_NAME),
                    Sort.Order.desc(InterfaceJobEntity_.CREATED_DATE_TIME)))
                .page(Pageable.unpaged()));

        assertEquals(1, page.getContent().size());
        InterfaceJobEntity result = page.getContent().getFirst();
        assertEquals(257401L, result.getInterfaceJobId());
        assertEquals("Luton", result.getBusinessUnit().getBusinessUnitName());
        assertEquals(InterfaceJobStatus.COMPLETED, result.getStatus());
        assertEquals(List.of("auto-payments-in-1.dat", "auto-payments-in-2.dat"),
            result.getInterfaceFiles().stream().map(interfaceFile -> interfaceFile.getFileName()).toList());
    }

    @Test
    @JiraStory("PO-2574")
    @JiraEpic("PO-304")
    void shouldReturnNoInterfaceJobsWhenBusinessUnitsAreNotPermitted() {
        InterfaceJobSearchCriteria searchCriteria = InterfaceJobSearchCriteria.builder()
            .permittedBusinessUnitIds(List.of((short) 9999))
            .statuses(List.of("COMPLETED"))
            .interfaceName("Auto Payments In")
            .build();

        Page<InterfaceJobEntity> page = interfaceJobRepository.findBy(
            specs.findBySearchCriteria(searchCriteria),
            ffq -> ffq.page(Pageable.unpaged()));

        assertTrue(page.isEmpty());
    }
}
