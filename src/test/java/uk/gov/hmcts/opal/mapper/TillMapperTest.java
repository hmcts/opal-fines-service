package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.TillSummaryEntity;

class TillMapperTest {

    private final TillMapper mapper = Mappers.getMapper(TillMapper.class);

    @Test
    void automaticTillReturnsJobId() {
        TillSummaryEntity till = TillSummaryEntity.builder().autoPayment(true).interfaceJobId(42L).build();
        assertEquals(42L, mapper.toResponse(till).getInterfaceJobId().orElse(null));
    }

    @Test
    void manualTillDoesNotReturnJobId() {
        TillSummaryEntity till = TillSummaryEntity.builder().autoPayment(false).interfaceJobId(42L).build();
        assertNull(mapper.toResponse(till).getInterfaceJobId().orElse(null));
    }

    @Test
    void automaticTillWithoutJobReturnsNull() {
        TillSummaryEntity till = TillSummaryEntity.builder().autoPayment(true).build();
        assertNull(mapper.toResponse(till).getInterfaceJobId().orElse(null));
    }
}
