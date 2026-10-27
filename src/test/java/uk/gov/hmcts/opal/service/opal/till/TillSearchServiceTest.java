package uk.gov.hmcts.opal.service.opal.till;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
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
import uk.gov.hmcts.opal.entity.InterfaceFileSourceEnum;
import uk.gov.hmcts.opal.entity.TillStatusEnum;
import uk.gov.hmcts.opal.entity.TillSummaryEntity;
import uk.gov.hmcts.opal.generated.model.TillsItem;
import uk.gov.hmcts.opal.generated.model.TillsResponse;
import uk.gov.hmcts.opal.mapper.TillMapper;
import uk.gov.hmcts.opal.repository.TillSummaryRepository;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.till.TillSearchService.TillSearchCriteria;

@ExtendWith(MockitoExtension.class)
class TillSearchServiceTest {

    @Mock
    private TillSummaryRepository tillSummaryRepository;

    @Mock
    private TillMapper tillMapper;

    @Mock
    private UserStateService userStateService;

    @Test
    void getTills_returnsEmptyResponseWhenNoFilterAndUserHasNoPermittedBusinessUnits() {
        TillSearchCriteria searchCriteria = TillSearchCriteria.builder().build();

        when(userStateService.getBusinessUnitIdsFor(
            FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(List.of());

        TillsResponse result = service().getTills(searchCriteria);

        assertEquals(List.of(), result.getTills());
        assertEquals(List.of(), searchCriteria.getPermittedBusinessUnitIds());
        verify(userStateService).getBusinessUnitIdsFor(
            FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);
        verifyNoInteractions(tillSummaryRepository, tillMapper);
    }

    @Test
    void getTills_rejectsSuppliedBusinessUnitWithoutPermission() {
        List<Short> requestedBusinessUnitIds = List.of((short) 78, (short) 80);
        TillSearchCriteria searchCriteria = TillSearchCriteria.builder()
            .businessUnitIds(requestedBusinessUnitIds)
            .build();

        when(userStateService.getPermittedBusinessUnitIds(
            requestedBusinessUnitIds,
            FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS
        )).thenReturn(List.of((short) 78));

        PermissionNotAllowedException exception = assertThrows(
            PermissionNotAllowedException.class,
            () -> service().getTills(searchCriteria)
        );

        assertEquals((short) 80, exception.getBusinessUnitId());
        verify(userStateService).getPermittedBusinessUnitIds(
            requestedBusinessUnitIds,
            FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS
        );
        verifyNoInteractions(tillSummaryRepository, tillMapper);
    }

    @SuppressWarnings("unchecked")
    @Test
    void getTills_returnsMappedTillsForSuppliedPermittedBusinessUnits() {
        List<Short> requestedBusinessUnitIds = List.of((short) 78, (short) 80);
        TillSearchCriteria searchCriteria = TillSearchCriteria.builder()
            .businessUnitIds(requestedBusinessUnitIds)
            .statuses(List.of("Allocated"))
            .autoPayments(true)
            .build();
        TillSummaryEntity entity = tillSummaryEntity();
        TillsItem responseItem = TillsItem.builder()
            .tillNumber((short) 12)
            .errors(2L)
            .amount(BigDecimal.valueOf(123.45))
            .businessUnitName("Luton")
            .processedBy("L078JG")
            .build();
        Page<TillSummaryEntity> mockPage = new PageImpl<>(List.of(entity), Pageable.unpaged(), 1);
        SpecificationFluentQuery<TillSummaryEntity> fluentQuery =
            (SpecificationFluentQuery<TillSummaryEntity>) mock(SpecificationFluentQuery.class);
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);

        when(userStateService.getPermittedBusinessUnitIds(
            requestedBusinessUnitIds,
            FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS
        )).thenReturn(requestedBusinessUnitIds);
        when(fluentQuery.sortBy(sortCaptor.capture())).thenReturn(fluentQuery);
        when(fluentQuery.page(Pageable.unpaged())).thenReturn(mockPage);
        when(tillSummaryRepository.findBy(
            any(Specification.class),
            any(Function.class)
        )).thenAnswer(invocation -> {
            Function<SpecificationFluentQuery<TillSummaryEntity>, Page<TillSummaryEntity>> queryFunction =
                invocation.getArgument(1);
            return queryFunction.apply(fluentQuery);
        });
        when(tillMapper.toResponse(entity)).thenReturn(responseItem);

        TillsResponse result = service().getTills(searchCriteria);

        assertEquals(List.of(responseItem), result.getTills());
        assertEquals(requestedBusinessUnitIds, searchCriteria.getPermittedBusinessUnitIds());
        assertEquals(List.of(TillStatusEnum.Allocated), searchCriteria.getTillStatuses());
        assertSummarySort(sortCaptor.getValue());
        verify(tillMapper).toResponse(entity);
    }

    @SuppressWarnings("unchecked")
    @Test
    void getTills_usesAllPermittedBusinessUnitsWhenNoFilterSupplied() {
        List<Short> permittedBusinessUnitIds = List.of((short) 78, (short) 80);
        TillSearchCriteria searchCriteria = TillSearchCriteria.builder().build();
        TillSummaryEntity entity = tillSummaryEntity();
        TillsItem responseItem = TillsItem.builder()
            .tillNumber((short) 12)
            .errors(2L)
            .amount(BigDecimal.valueOf(123.45))
            .businessUnitName("Luton")
            .processedBy("L078JG")
            .build();
        Page<TillSummaryEntity> mockPage = new PageImpl<>(List.of(entity), Pageable.unpaged(), 1);
        SpecificationFluentQuery<TillSummaryEntity> fluentQuery =
            (SpecificationFluentQuery<TillSummaryEntity>) mock(SpecificationFluentQuery.class);

        when(userStateService.getBusinessUnitIdsFor(
            FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS
        )).thenReturn(permittedBusinessUnitIds);
        when(fluentQuery.sortBy(any(Sort.class))).thenReturn(fluentQuery);
        when(fluentQuery.page(Pageable.unpaged())).thenReturn(mockPage);
        when(tillSummaryRepository.findBy(
            any(Specification.class),
            any(Function.class)
        )).thenAnswer(invocation -> {
            Function<SpecificationFluentQuery<TillSummaryEntity>, Page<TillSummaryEntity>> queryFunction =
                invocation.getArgument(1);
            return queryFunction.apply(fluentQuery);
        });
        when(tillMapper.toResponse(entity)).thenReturn(responseItem);

        TillsResponse result = service().getTills(searchCriteria);

        assertEquals(List.of(responseItem), result.getTills());
        assertEquals(permittedBusinessUnitIds, searchCriteria.getPermittedBusinessUnitIds());
        verify(userStateService).getBusinessUnitIdsFor(
            FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS
        );
        verify(tillMapper).toResponse(entity);
    }

    @Test
    void getTillStatuses_rejectsUnknownStatus() {
        TillSearchCriteria searchCriteria = TillSearchCriteria.builder()
            .statuses(List.of("unknown"))
            .build();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, searchCriteria::getTillStatuses);

        assertEquals("Unknown till status: unknown", exception.getMessage());
    }

    private TillSearchService service() {
        return new TillSearchService(tillSummaryRepository, tillMapper, userStateService);
    }

    private TillSummaryEntity tillSummaryEntity() {
        return TillSummaryEntity.builder()
            .tillId(257501L)
            .tillNumber((short) 12)
            .errors(2L)
            .fileName("auto-payments.dat")
            .source(InterfaceFileSourceEnum.NATWEST)
            .amount(BigDecimal.valueOf(123.45))
            .businessUnitId((short) 78)
            .businessUnitName("Luton")
            .processedBy("L078JG")
            .dateProcessed(LocalDateTime.of(2026, Month.AUGUST, 27, 9, 30))
            .autoPayment(true)
            .status(TillStatusEnum.Allocated)
            .build();
    }

    private void assertSummarySort(Sort sort) {
        assertEquals(Sort.Direction.ASC, sort.getOrderFor("businessUnitName").getDirection());
        assertEquals(Sort.Direction.DESC, sort.getOrderFor("dateProcessed").getDirection());
    }
}
