package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.SuspenseItemSummaryEntity;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryLiteCommon;
import uk.gov.hmcts.opal.generated.model.SuspenseItemSummaryCommonStrict;
import uk.gov.hmcts.opal.mapper.SuspenseItemSummaryMapper;
import uk.gov.hmcts.opal.repository.SuspenseItemSummaryRepository;

@ExtendWith(MockitoExtension.class)
public class OpalSuspenseAccountSummaryServiceTest {

    @Mock
    private SuspenseItemSummaryRepository suspenseItemSummaryRepository;

    @InjectMocks
    private OpalSuspenseAccountsSummaryService opalSuspenseAccountsSummaryService;

    @Mock
    private SuspenseItemSummaryMapper suspenseItemSummaryMapper;

    @Test
    public void testSuspenseItemSummaryService() {
        //  Set up.
        SuspenseItemSummaryEntity returnedEntity = SuspenseItemSummaryEntity
            .builder()
            .id(1L)
            .suspenseItemNumber(BigInteger.valueOf(12345))
            .businessUnitId((short) 1)
            .businessUnitName("Business Unit Name")
            .createdDate(LocalDateTime.parse("2026-11-03T10:15:30"))
            .balance(BigDecimal.valueOf(100.00))
            .suspenseItemNumber(BigInteger.valueOf(12345))
            .businessUnitId((short) 123)
            .build();

        SuspenseItemSummaryCommonStrict expected = SuspenseItemSummaryCommonStrict
            .builder()
            .suspenseItemId(1L)
            .suspenseItemNumber(12345L)
            .businessUnitDetails(BusinessUnitSummaryLiteCommon
                .builder()
                .businessUnitId((short) 1)
                .businessUnitName("Business Unit Name")
                .build())
            .createdDate(LocalDateTime.parse("2026-11-03T10:15:30"))
            .balance(BigDecimal.valueOf(100.0))
            .build();

        doReturn(returnedEntity).when(suspenseItemSummaryRepository)
            .findByBusinessUnitIdAndSuspenseItemNumber(any(), any());

        doReturn(expected).when(suspenseItemSummaryMapper)
            .toSuspenseItemSummaryCommonStrict(returnedEntity);

        //  Execute.
        SuspenseItemSummaryCommonStrict returnedCommonStrict =
            opalSuspenseAccountsSummaryService.getSuspenseAccountsSummary((short) 123, BigInteger.valueOf(123L));

        //  Verify.
        assert returnedCommonStrict.getSuspenseItemId() != null;
        assert returnedCommonStrict.getBusinessUnitDetails() != null;
        assert returnedCommonStrict.getCreatedDate() != null;

        assertEquals(1L, returnedCommonStrict.getSuspenseItemId().intValue());
        assertEquals((short) 12345, returnedCommonStrict.getSuspenseItemNumber());
        assertEquals(1L, returnedCommonStrict.getBusinessUnitDetails().getBusinessUnitId().intValue());
        assertEquals("Business Unit Name",
            returnedCommonStrict.getBusinessUnitDetails().getBusinessUnitName());
        assertEquals("2026/11/03",
            returnedCommonStrict.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
        assertEquals(BigDecimal.valueOf(100.00), returnedCommonStrict.getBalance());
    }

    @Test
    void getSummary_returnsMappedItem() {
        SuspenseItemSummaryEntity entity = SuspenseItemSummaryEntity.builder()
            .id(1L)
            .suspenseItemNumber(BigInteger.valueOf(12345))
            .businessUnitId((short) 123)
            .build();
        SuspenseItemSummaryCommonStrict expected = SuspenseItemSummaryCommonStrict.builder()
            .suspenseItemId(1L)
            .suspenseItemNumber(12345L)
            .balance(BigDecimal.valueOf(100.0))
            .build();
        when(suspenseItemSummaryRepository.findByBusinessUnitIdAndSuspenseItemNumber((short) 123,
            BigInteger.valueOf(12345)))
            .thenReturn(entity);
        when(suspenseItemSummaryMapper.toSuspenseItemSummaryCommonStrict(entity)).thenReturn(expected);

        SuspenseItemSummaryCommonStrict result =
            opalSuspenseAccountsSummaryService.getSuspenseAccountsSummary((short) 123,
                BigInteger.valueOf(12345));

        assertSame(expected, result);
        verify(suspenseItemSummaryRepository).findByBusinessUnitIdAndSuspenseItemNumber((short) 123,
            BigInteger.valueOf(12345));
        verify(suspenseItemSummaryMapper).toSuspenseItemSummaryCommonStrict(entity);
    }

    @Test
    void getSummary_returnsNullForMissingItem() {
        when(suspenseItemSummaryRepository.findByBusinessUnitIdAndSuspenseItemNumber((short) 123,
            BigInteger.valueOf(12345)))
            .thenReturn(null);

        assertNull(opalSuspenseAccountsSummaryService.getSuspenseAccountsSummary((short) 123,
            BigInteger.valueOf(12345)));
        verify(suspenseItemSummaryRepository).findByBusinessUnitIdAndSuspenseItemNumber((short) 123,
            BigInteger.valueOf(12345));
        verify(suspenseItemSummaryMapper).toSuspenseItemSummaryCommonStrict(null);
    }

}

