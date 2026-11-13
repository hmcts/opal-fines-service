package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.SuspenseItemSummaryEntity;
import uk.gov.hmcts.opal.generated.model.SuspenseItemSummaryCommonStrict;
import uk.gov.hmcts.opal.mapper.SuspenseItemSummaryMapper;
import uk.gov.hmcts.opal.repository.SuspenseItemSummaryRepository;

@ExtendWith(MockitoExtension.class)
class OpalSuspenseAccountSummaryServiceTest {

    @Mock
    private SuspenseItemSummaryRepository suspenseItemSummaryRepository;

    @InjectMocks
    private OpalSuspenseAccountsSummaryService opalSuspenseAccountsSummaryService;

    @Mock
    private SuspenseItemSummaryMapper suspenseItemSummaryMapper;

    @Test
    void getSummary_returnsMappedItem() {
        SuspenseItemSummaryEntity entity = SuspenseItemSummaryEntity.builder()
            .id(1L)
            .suspenseItemNumber(12345L)
            .businessUnitId((short) 123)
            .build();
        SuspenseItemSummaryCommonStrict expected = SuspenseItemSummaryCommonStrict.builder()
            .suspenseItemId(1L)
            .suspenseItemNumber(12345L)
            .balance("100.0")
            .build();
        when(suspenseItemSummaryRepository.findByBusinessUnitIdAndSuspenseItemNumber((short) 123, 12345L))
            .thenReturn(entity);
        when(suspenseItemSummaryMapper.toSuspenseItemSummaryCommonStrict(entity)).thenReturn(expected);

        SuspenseItemSummaryCommonStrict result =
            opalSuspenseAccountsSummaryService.getSuspenseAccountsSummary((short) 123, 12345L);

        assertSame(expected, result);
        verify(suspenseItemSummaryRepository).findByBusinessUnitIdAndSuspenseItemNumber((short) 123, 12345L);
        verify(suspenseItemSummaryMapper).toSuspenseItemSummaryCommonStrict(entity);
    }

    @Test
    void getSummary_returnsNullForMissingItem() {
        when(suspenseItemSummaryRepository.findByBusinessUnitIdAndSuspenseItemNumber((short) 123, 12345L))
            .thenReturn(null);

        assertNull(opalSuspenseAccountsSummaryService.getSuspenseAccountsSummary((short) 123, 12345L));
        verify(suspenseItemSummaryRepository).findByBusinessUnitIdAndSuspenseItemNumber((short) 123, 12345L);
        verify(suspenseItemSummaryMapper).toSuspenseItemSummaryCommonStrict(null);
    }

}

