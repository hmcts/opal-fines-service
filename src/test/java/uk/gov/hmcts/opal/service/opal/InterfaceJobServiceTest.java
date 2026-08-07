package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor.SpecificationFluentQuery;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateItem;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateRequest;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateResponse;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsCreateResponseItem;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsFileSource;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsJobStatus;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsProcessItem;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsProcessRequest;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsSummaryItem;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsSummaryResponse;
import uk.gov.hmcts.opal.mapper.InterfaceJobMapper;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.repository.InterfaceFileRepository;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.InterfaceJobService.InterfaceJobSearchCriteria;
import uk.gov.hmcts.opal.service.messaging.InterfaceJobQueuePublisher;

@ExtendWith(MockitoExtension.class)
class InterfaceJobServiceTest {

    @Mock
    private InterfaceJobRepository interfaceJobRepository;

    @Mock
    private InterfaceFileRepository interfaceFileRepository;

    @Mock
    private InterfaceJobMapper interfaceJobMapper;

    @Mock
    private BusinessUnitService businessUnitService;

    @Mock
    private UserStateService userStateService;

    @Mock
    private InterfaceJobQueuePublisher interfaceJobQueuePublisher;

    private InterfaceJobService interfaceJobService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-07-21T09:15:00Z"), ZoneOffset.UTC);
        interfaceJobService = new InterfaceJobService(interfaceJobRepository, interfaceFileRepository,
            interfaceJobMapper, businessUnitService, userStateService, interfaceJobQueuePublisher, clock);
    }

    @Test
    void create_savesJobsAndFiles() {
        LocalDateTime createdDateTime = LocalDateTime.of(2026, 7, 14, 10, 0);
        InterfaceJobsCreateItem requestItem = InterfaceJobsCreateItem.builder()
            .fileName("auto-payments-in.dat")
            .source(InterfaceJobsFileSource.NATWEST)
            .records("[{\"account\":\"123\"}]")
            .businessUnitId((short) 77)
            .interfaceName("Auto Payments In")
            .createdDatetime(createdDateTime)
            .build();
        InterfaceJobsCreateRequest request = InterfaceJobsCreateRequest.builder()
            .interfaceJobs(List.of(requestItem))
            .build();
        BusinessUnitEntity businessUnit = BusinessUnitEntity.builder().businessUnitId((short) 77).build();
        InterfaceJobEntity unsavedJob = InterfaceJobEntity.builder().businessUnit(businessUnit).build();
        InterfaceJobEntity savedJob = InterfaceJobEntity.builder()
            .interfaceJobId(123L)
            .businessUnit(businessUnit)
            .build();
        InterfaceFileEntity unsavedFile = InterfaceFileEntity.builder()
            .interfaceJob(savedJob)
            .fileName("auto-payments-in.dat")
            .records("[{\"account\":\"123\"}]")
            .build();
        InterfaceJobsCreateResponseItem createResponse = InterfaceJobsCreateResponseItem.builder()
            .interfaceJobId(123L)
            .build();

        when(userStateService.getPermittedBusinessUnitIds(
            List.of((short) 77), FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(List.of((short) 77));
        when(businessUnitService.getBusinessUnit((short) 77)).thenReturn(businessUnit);
        when(interfaceJobMapper.toJobEntity(requestItem, businessUnit)).thenReturn(unsavedJob);
        when(interfaceJobRepository.save(unsavedJob)).thenReturn(savedJob);
        when(interfaceJobMapper.toFileEntity(requestItem, savedJob)).thenReturn(unsavedFile);
        when(interfaceJobMapper.toCreateResponse(savedJob)).thenReturn(createResponse);

        InterfaceJobsCreateResponse result = interfaceJobService.create(request);

        assertEquals(List.of(createResponse), result.getInterfaceJobs());
        verify(userStateService).getPermittedBusinessUnitIds(
            List.of((short) 77), FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);
        verify(businessUnitService).getBusinessUnit((short) 77);
        verify(interfaceJobRepository).save(unsavedJob);
        verify(interfaceFileRepository).save(unsavedFile);
        verify(interfaceJobMapper).toCreateResponse(savedJob);
    }

    @Test
    void create_rejectsUserWithoutPermission() {
        InterfaceJobsCreateItem requestItem = InterfaceJobsCreateItem.builder()
            .businessUnitId((short) 77)
            .build();
        InterfaceJobsCreateRequest request = InterfaceJobsCreateRequest.builder()
            .interfaceJobs(List.of(requestItem))
            .build();

        when(userStateService.getPermittedBusinessUnitIds(
            List.of((short) 77), FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(List.of());

        PermissionNotAllowedException exception = assertThrows(
            PermissionNotAllowedException.class, () -> interfaceJobService.create(request));

        assertEquals("[PROCESS_AND_ALLOCATE_PAYMENTS] permission(s) are not enabled for the user in business unit: 77",
            exception.getMessage());
        verify(userStateService).getPermittedBusinessUnitIds(
            List.of((short) 77), FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);
        verify(businessUnitService, never()).getBusinessUnit((short) 77);
        verifyNoInteractions(interfaceJobRepository);
        verifyNoInteractions(interfaceFileRepository);
        verifyNoInteractions(interfaceJobMapper);
    }

    @Test
    void getSummary_returnsEmptyResponseWhenUserHasNoPermittedBusinessUnits() {
        List<Short> requestedBusinessUnitIds = List.of((short) 10);
        InterfaceJobSearchCriteria searchCriteria = InterfaceJobSearchCriteria.builder()
            .businessUnitIds(requestedBusinessUnitIds)
            .build();

        when(userStateService.getPermittedBusinessUnitIds(
            requestedBusinessUnitIds, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(List.of());

        InterfaceJobsSummaryResponse result = interfaceJobService.getSummary(searchCriteria);

        assertEquals(List.of(), result.getInterfaceJobs());
        assertEquals(List.of(), searchCriteria.getPermittedBusinessUnitIds());
        verify(userStateService).getPermittedBusinessUnitIds(
            requestedBusinessUnitIds, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);
        verifyNoInteractions(interfaceJobRepository);
        verifyNoInteractions(interfaceJobMapper);
    }

    @SuppressWarnings("unchecked")
    @Test
    void getSummary_returnsMappedSummariesForPermittedBusinessUnits() {
        List<Short> requestedBusinessUnitIds = List.of((short) 10, (short) 20);
        LocalDateTime completedDateFrom = LocalDateTime.of(2026, Month.JULY, 1, 9, 0);
        LocalDateTime completedDateTo = LocalDateTime.of(2026, Month.JULY, 2, 17, 0);
        InterfaceJobSearchCriteria searchCriteria = InterfaceJobSearchCriteria.builder()
            .businessUnitIds(requestedBusinessUnitIds)
            .statuses(List.of("COMPLETED"))
            .completedDateFrom(completedDateFrom)
            .completedDateTo(completedDateTo)
            .interfaceName("Auto Payments In")
            .build();

        InterfaceFileEntity firstFile = InterfaceFileEntity.builder().interfaceFileId(101L).build();
        InterfaceFileEntity secondFile = InterfaceFileEntity.builder().interfaceFileId(102L).build();
        InterfaceJobEntity interfaceJob = InterfaceJobEntity.builder()
            .interfaceJobId(1L)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 20).businessUnitName("Luton").build())
            .interfaceFiles(List.of(firstFile, secondFile))
            .build();
        Page<InterfaceJobEntity> mockPage = new PageImpl<>(List.of(interfaceJob), Pageable.unpaged(), 1);

        SpecificationFluentQuery<InterfaceJobEntity> fluentQuery =
            (SpecificationFluentQuery<InterfaceJobEntity>) mock(SpecificationFluentQuery.class);
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        when(fluentQuery.sortBy(sortCaptor.capture())).thenReturn(fluentQuery);
        when(fluentQuery.page(Pageable.unpaged())).thenReturn(mockPage);
        when(userStateService.getPermittedBusinessUnitIds(
            requestedBusinessUnitIds, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS))
            .thenReturn(List.of((short) 20));
        when(interfaceJobRepository.findBy(any(Specification.class), any(Function.class))).thenAnswer(invocation -> {
            Function<SpecificationFluentQuery<InterfaceJobEntity>, Page<InterfaceJobEntity>> queryFunction =
                invocation.getArgument(1);
            return queryFunction.apply(fluentQuery);
        });

        InterfaceJobsSummaryItem firstResponse = summaryResponse(1L, 101L, "first.dat");
        InterfaceJobsSummaryItem secondResponse = summaryResponse(1L, 102L, "second.dat");
        when(interfaceJobMapper.toSummaryResponse(interfaceJob, firstFile)).thenReturn(firstResponse);
        when(interfaceJobMapper.toSummaryResponse(interfaceJob, secondFile)).thenReturn(secondResponse);

        InterfaceJobsSummaryResponse result = interfaceJobService.getSummary(searchCriteria);

        assertEquals(List.of(firstResponse, secondResponse), result.getInterfaceJobs());
        assertEquals(List.of((short) 20), searchCriteria.getPermittedBusinessUnitIds());
        assertSummarySort(sortCaptor.getValue());
        verify(interfaceJobRepository).findBy(any(Specification.class), any(Function.class));
        verify(userStateService).getPermittedBusinessUnitIds(
            requestedBusinessUnitIds, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);
        verify(interfaceJobMapper).toSummaryResponse(interfaceJob, firstFile);
        verify(interfaceJobMapper).toSummaryResponse(interfaceJob, secondFile);
    }

    @Test
    void process_updatesEligibleJobAndPublishesItsId() {
        InterfaceJobEntity job = processJob(123L, InterfaceJobStatus.CREATED);
        InterfaceFileEntity file = InterfaceFileEntity.builder().interfaceJob(job).build();
        job.setInterfaceFiles(List.of(file));
        when(interfaceJobRepository.findAllByInterfaceJobIdIn(List.of(123L))).thenReturn(List.of(job));
        permitProcessingBusinessUnit();

        interfaceJobService.process(processRequest(123L, BUSINESS_UNIT_ID, true));

        assertEquals(InterfaceJobStatus.PROCESSING, job.getStatus());
        assertEquals(LocalDateTime.of(2026, 7, 21, 9, 15), job.getStartedDateTime());
        assertTrue(file.isOverrideInhibits());
        verify(interfaceJobQueuePublisher).publish(List.of(123L));
    }

    @Test
    void process_allowsFailedJobs() {
        InterfaceJobEntity job = processJob(123L, InterfaceJobStatus.FAILED);
        job.setInterfaceFiles(List.of(InterfaceFileEntity.builder().interfaceJob(job).build()));
        when(interfaceJobRepository.findAllByInterfaceJobIdIn(List.of(123L))).thenReturn(List.of(job));
        permitProcessingBusinessUnit();

        interfaceJobService.process(processRequest(123L, BUSINESS_UNIT_ID, false));

        assertEquals(InterfaceJobStatus.PROCESSING, job.getStatus());
        assertFalse(job.getInterfaceFiles().getFirst().isOverrideInhibits());
        verify(interfaceJobQueuePublisher).publish(List.of(123L));
    }

    @Test
    void process_rejectsMissingJobWithoutPublishing() {
        when(interfaceJobRepository.findAllByInterfaceJobIdIn(List.of(123L))).thenReturn(List.of());

        assertThrows(jakarta.persistence.EntityNotFoundException.class,
            () -> interfaceJobService.process(processRequest(123L, BUSINESS_UNIT_ID, true)));

        verifyNoInteractions(interfaceJobQueuePublisher, userStateService);
    }

    @Test
    void process_rejectsInvalidStatusWithoutChangingOrPublishing() {
        InterfaceJobEntity job = processJob(123L, InterfaceJobStatus.PROCESSING);
        when(interfaceJobRepository.findAllByInterfaceJobIdIn(List.of(123L))).thenReturn(List.of(job));
        permitProcessingBusinessUnit();

        assertThrows(ResourceConflictException.class,
            () -> interfaceJobService.process(processRequest(123L, BUSINESS_UNIT_ID, true)));

        assertEquals(InterfaceJobStatus.PROCESSING, job.getStatus());
        verifyNoInteractions(interfaceJobQueuePublisher);
    }

    @Test
    void process_rejectsBusinessUnitWithoutPermission() {
        InterfaceJobEntity job = processJob(123L, InterfaceJobStatus.CREATED);
        when(interfaceJobRepository.findAllByInterfaceJobIdIn(List.of(123L))).thenReturn(List.of(job));
        when(userStateService.getPermittedBusinessUnitIds(
            List.of(BUSINESS_UNIT_ID), FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(List.of());

        assertThrows(PermissionNotAllowedException.class,
            () -> interfaceJobService.process(processRequest(123L, BUSINESS_UNIT_ID, true)));

        assertEquals(InterfaceJobStatus.CREATED, job.getStatus());
        verifyNoInteractions(interfaceJobQueuePublisher);
    }

    @Test
    void process_rejectsBusinessUnitMismatchWithoutPublishing() {
        InterfaceJobEntity job = processJob(123L, InterfaceJobStatus.CREATED);
        when(interfaceJobRepository.findAllByInterfaceJobIdIn(List.of(123L))).thenReturn(List.of(job));

        assertThrows(IllegalArgumentException.class,
            () -> interfaceJobService.process(processRequest(123L, (short) 72, true)));

        verifyNoInteractions(interfaceJobQueuePublisher, userStateService);
    }

    private InterfaceJobsSummaryItem summaryResponse(
        Long interfaceJobId, Long interfaceFileId, String fileName) {
        LocalDateTime now = LocalDateTime.now();

        return InterfaceJobsSummaryItem.builder()
            .interfaceJobId(interfaceJobId)
            .interfaceFileId(interfaceFileId)
            .fileName(fileName)
            .source(InterfaceJobsFileSource.NATWEST)
            .businessUnitName("Luton")
            .completedDatetime(now)
            .createdDatetime(now)
            .status(InterfaceJobsJobStatus.COMPLETED)
            .build();
    }

    private void assertSummarySort(Sort sort) {
        List<Sort.Order> orders = sort.stream().toList();

        assertEquals(2, orders.size());
        assertEquals("businessUnit.businessUnitName", orders.get(0).getProperty());
        assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());
        assertEquals("createdDateTime", orders.get(1).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(1).getDirection());
    }

    private void permitProcessingBusinessUnit() {
        when(userStateService.getPermittedBusinessUnitIds(
            List.of(BUSINESS_UNIT_ID), FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS))
            .thenReturn(List.of(BUSINESS_UNIT_ID));
    }

    private InterfaceJobEntity processJob(Long id, InterfaceJobStatus status) {
        return InterfaceJobEntity.builder()
            .interfaceJobId(id)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId(BUSINESS_UNIT_ID).build())
            .status(status)
            .build();
    }

    private InterfaceJobsProcessRequest processRequest(Long id, Short businessUnitId, boolean overrideInhibits) {
        return InterfaceJobsProcessRequest.builder()
            .interfaceJobs(List.of(InterfaceJobsProcessItem.builder()
                .interfaceJobId(id)
                .businessUnitId(businessUnitId)
                .overrideInhibits(overrideInhibits)
                .build()))
            .build();
    }

    private static final Short BUSINESS_UNIT_ID = 71;

}
