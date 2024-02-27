package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import uk.gov.hmcts.opal.dto.search.FixedPenaltyOffenceSearchDto;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class LegacyFixedPenaltyOffenceServiceTest extends LegacyTestsBase {

    @Mock
    private Logger log;

    @InjectMocks
    private LegacyFixedPenaltyOffenceService legacyFixedPenaltyOffenceService;

    @Test
    void testGetFixedPenaltyOffence() {
        // Arrange

        FixedPenaltyOffenceEntity fixedPenaltyOffenceEntity = FixedPenaltyOffenceEntity.builder().build();

        // Act
        LegacyGatewayResponseException exception = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyFixedPenaltyOffenceService.getFixedPenaltyOffence(1)
        );

        // Assert
        assertNotNull(legacyFixedPenaltyOffenceService.getLog());
        assertNotNull(exception);
        assertEquals(NOT_YET_IMPLEMENTED, exception.getMessage());

    }

    @Test
    void testSearchFixedPenaltyOffences() {
        // Arrange

        FixedPenaltyOffenceEntity fixedPenaltyOffenceEntity = FixedPenaltyOffenceEntity.builder().build();

        // Act
        LegacyGatewayResponseException exception = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyFixedPenaltyOffenceService.searchFixedPenaltyOffences(
                FixedPenaltyOffenceSearchDto.builder().build())
        );

        // Assert
        assertNotNull(exception);
        assertEquals(NOT_YET_IMPLEMENTED, exception.getMessage());

    }

}
