package uk.gov.hmcts.opal.mapper.suspense;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.SuspenseItemSummaryEntity;
import uk.gov.hmcts.opal.generated.model.SuspenseItemSummaryCommonStrict;
import uk.gov.hmcts.opal.mapper.SuspenseItemSummaryMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SuspenseItemSummaryMapperTest {

    private final SuspenseItemSummaryMapper mapper = Mappers.getMapper(SuspenseItemSummaryMapper.class);

    @Test
    void testSuspenseItemSummaryMapper() throws Exception {
        SuspenseItemSummaryEntity entity = SuspenseItemSummaryEntity
            .builder()
            .id(1L)
            .suspenseItemNumber((short) 12345)
            .suspenseItemType("CC")
            .businessUnitId(1L)
            .businessUnitName("Business Unit Name")
            .createdDate("2026-11-03T10:15:30")
            .balance(BigDecimal.valueOf(10000L))
            .build();

        SuspenseItemSummaryCommonStrict result = mapper.toSuspenseItemSummaryCommonStrict(entity);

        assert result.getSuspenseItemId() != null;
        assert result.getBusinessUnitDetails() != null;
        assert result.getCreatedDate() != null;
        assert result.getSuspenseItemType() != null;

        assertEquals(1L, result.getSuspenseItemId().intValue());
        assertEquals((short) 12345, result.getSuspenseItemNumber());
        assertEquals("CC", result.getSuspenseItemType().getItemType());
        assertEquals("Cancelled cheque (Fines)",  result.getSuspenseItemType().getItemTypeDisplayName());
        assertEquals(1L, result.getBusinessUnitDetails().getBusinessUnitId().intValue());
        assertEquals("Business Unit Name", result.getBusinessUnitDetails().getBusinessUnitName());
        assertEquals("2026/11/03", result.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
        assertEquals("10000", result.getBalance().get());
    }

}
