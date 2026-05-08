package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.mapper.AbstractMapperTest;

class LegacyBusinessUnitSummaryMapperTest extends AbstractMapperTest {

    @Autowired
    private LegacyBusinessUnitSummaryMapper mapper;

    @Test
    void givenLegacyBusinessUnitSummary_whenToOpal_thenMapsExpectedFields() {
        // Arrange
        uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary legacy =
            uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary.builder()
                .businessUnitId("77")
                .businessUnitName("Camberwell Green")
                .businessUnitCode("CBG")
                .welshSpeaking("Y")
                .build();

        // Act
        BusinessUnitSummary mapped = mapper.toOpal(legacy);

        // Assert
        assertNotNull(mapped);
        assertEquals("77", mapped.getBusinessUnitId());
        assertEquals("Camberwell Green", mapped.getBusinessUnitName());
        assertEquals("Y", mapped.getWelshSpeaking());
    }

    @Test
    void givenNullLegacyBusinessUnitSummary_whenToOpal_thenReturnsNull() {
        assertNull(mapper.toOpal(null));
    }
}
