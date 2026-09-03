package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.entity.businessunit.OutstandingAutoPaymentEntity;
import uk.gov.hmcts.opal.generated.model.BusinessUnitsOutstandingAutoPaymentItem;
import uk.gov.hmcts.opal.generated.model.BusinessUnitsOutstandingAutoPaymentResponse;
import uk.gov.hmcts.opal.mapper.OutstandingAutoPaymentMapper;
import uk.gov.hmcts.opal.repository.OutstandingAutoPaymentRepository;
import uk.gov.hmcts.opal.service.UserStateService;

@ExtendWith(MockitoExtension.class)
class OutstandingAutoPaymentServiceTest {

    @Mock
    private OutstandingAutoPaymentRepository repository;

    @Mock
    private OutstandingAutoPaymentMapper mapper;

    @Mock
    private UserStateService userStateService;

    @InjectMocks
    private OutstandingAutoPaymentService service;

    @Test
    void getOutstandingAutoPaymentCount_returnsEmptyResponseWhenNoBusinessUnitsPermitted() {
        when(userStateService.getBusinessUnitIdsFor(
            FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(List.of());

        BusinessUnitsOutstandingAutoPaymentResponse response = service.getOutstandingAutoPaymentCount();

        assertEquals(List.of(), response.getBusinessUnits());
        verify(userStateService).getBusinessUnitIdsFor(FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);
        verifyNoInteractions(repository);
        verifyNoInteractions(mapper);
    }

    @Test
    void getOutstandingAutoPaymentCount_returnsMappedCountsForPermittedBusinessUnits() {
        List<Short> permittedBusinessUnitIds = List.of((short) 77, (short) 78);
        OutstandingAutoPaymentEntity firstEntity = OutstandingAutoPaymentEntity.builder()
            .businessUnitId((short) 77)
            .businessUnitName("Luton")
            .filesToProcessCount(2L)
            .tillsToAllocateCount(1L)
            .build();
        OutstandingAutoPaymentEntity secondEntity = OutstandingAutoPaymentEntity.builder()
            .businessUnitId((short) 78)
            .businessUnitName("Cardiff")
            .filesToProcessCount(1L)
            .tillsToAllocateCount(3L)
            .build();
        List<OutstandingAutoPaymentEntity> entities = List.of(firstEntity, secondEntity);
        BusinessUnitsOutstandingAutoPaymentItem firstItem = BusinessUnitsOutstandingAutoPaymentItem.builder()
            .businessUnitId((short) 77)
            .businessUnitName("Luton")
            .fileCount(2L)
            .tillCount(1L)
            .build();
        BusinessUnitsOutstandingAutoPaymentItem secondItem = BusinessUnitsOutstandingAutoPaymentItem.builder()
            .businessUnitId((short) 78)
            .businessUnitName("Cardiff")
            .fileCount(1L)
            .tillCount(3L)
            .build();

        when(userStateService.getBusinessUnitIdsFor(
            FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(permittedBusinessUnitIds);
        when(repository.findByBusinessUnitIdInOrderByBusinessUnitNameAsc(permittedBusinessUnitIds))
            .thenReturn(entities);
        when(mapper.toItems(entities)).thenReturn(List.of(firstItem, secondItem));

        BusinessUnitsOutstandingAutoPaymentResponse response = service.getOutstandingAutoPaymentCount();

        assertEquals(List.of(firstItem, secondItem), response.getBusinessUnits());
        verify(userStateService).getBusinessUnitIdsFor(FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);
        verify(repository).findByBusinessUnitIdInOrderByBusinessUnitNameAsc(permittedBusinessUnitIds);
        verify(mapper).toItems(entities);
    }
}
