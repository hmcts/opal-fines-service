package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor.SpecificationFluentQuery;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsFileSource;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsJobStatus;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsSummaryItem;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsSummaryResponse;
import uk.gov.hmcts.opal.mapper.InterfaceJobMapper;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.InterfaceJobService.InterfaceJobSearchCriteria;

@ExtendWith(MockitoExtension.class)
class InterfaceJobServiceTest {

    @Mock
    private InterfaceJobRepository interfaceJobRepository;

    @Mock
    private InterfaceJobMapper interfaceJobMapper;

    @Mock
    private UserStateService userStateService;

    @InjectMocks
    private InterfaceJobService interfaceJobService;

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
        LocalDateTime completedDateFrom = LocalDateTime.of(2026, 7, 1, 9, 0);
        LocalDateTime completedDateTo = LocalDateTime.of(2026, 7, 2, 17, 0);
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
            (SpecificationFluentQuery<InterfaceJobEntity>) Mockito.mock(SpecificationFluentQuery.class);
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

}
