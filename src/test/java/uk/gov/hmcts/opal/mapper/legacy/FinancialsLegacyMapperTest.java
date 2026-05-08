package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse.Financials;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHeaderSummaryResponse.FinancialsLegacy;
import uk.gov.hmcts.opal.mapper.AbstractMapperTest;

class FinancialsLegacyMapperTest extends AbstractMapperTest {

    @Autowired
    private FinancialsLegacyMapper mapper;

    @Test
    void givenLegacyFinancials_whenToOpal_thenMapsExpectedFields() {
        // Arrange
        FinancialsLegacy legacy = FinancialsLegacy.builder()
            .awarded(BigDecimal.valueOf(100.25))
            .paidOut(BigDecimal.valueOf(20.50))
            .awaitingPayout(BigDecimal.valueOf(30.75))
            .outstanding(BigDecimal.valueOf(49.00))
            .build();

        // Act
        Financials mapped = mapper.toOpal(legacy);

        // Assert
        assertNotNull(mapped);
        assertEquals(BigDecimal.valueOf(100.25), mapped.getAwarded());
        assertEquals(BigDecimal.valueOf(20.50), mapped.getPaidOut());
        assertEquals(BigDecimal.valueOf(30.75), mapped.getAwaitingPayout());
        assertEquals(BigDecimal.valueOf(49.00), mapped.getOutstanding());
    }

    @Test
    void givenNullLegacyFinancials_whenToOpal_thenReturnsNull() {
        assertNull(mapper.toOpal(null));
    }
}
