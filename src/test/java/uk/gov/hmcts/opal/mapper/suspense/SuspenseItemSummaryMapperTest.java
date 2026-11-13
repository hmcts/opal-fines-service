package uk.gov.hmcts.opal.mapper.suspense;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.SuspenseItemSummaryEntity;
import uk.gov.hmcts.opal.generated.model.SuspenseItemSummaryCommonStrict;
import uk.gov.hmcts.opal.mapper.SuspenseItemSummaryMapper;

class SuspenseItemSummaryMapperTest {

    private final SuspenseItemSummaryMapper mapper = Mappers.getMapper(SuspenseItemSummaryMapper.class);

    @Test
    void testSuspenseItemSummaryMapper() {
        SuspenseItemSummaryEntity entity = SuspenseItemSummaryEntity
            .builder()
            .id(1L)
            .suspenseItemNumber(12345L)
            .suspenseItemType("Payment")
            .businessUnitId((short) 1)
            .businessUnitName("Business Unit Name")
            .createdDate(LocalDateTime.parse("2026-11-03T10:15:30"))
            .balance(100.0)
            .build();

        SuspenseItemSummaryCommonStrict result = mapper.toSuspenseItemSummaryCommonStrict(entity);

        assertNotNull(result.getSuspenseItemId());
        assertNotNull(result.getBusinessUnitDetails());
        assertNotNull(result.getCreatedDate());

        assertEquals(1L, result.getSuspenseItemId());
        assertEquals(12345L, result.getSuspenseItemNumber());
        assertEquals((short) 1, result.getBusinessUnitDetails().getBusinessUnitId());
        assertEquals("Business Unit Name", result.getBusinessUnitDetails().getBusinessUnitName());
        assertEquals("2026/11/03", result.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
        assertEquals("100.0", result.getBalance().get());
        assertEquals("Payment", result.getSuspenseItemType().get());
    }

    @Test
    void toSummary_returnsNullForMissingEntity() {
        assertNull(mapper.toSuspenseItemSummaryCommonStrict(null));
    }

}
