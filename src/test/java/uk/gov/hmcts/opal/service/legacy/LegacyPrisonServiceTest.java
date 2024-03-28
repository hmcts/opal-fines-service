package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import uk.gov.hmcts.opal.dto.search.PrisonSearchDto;
import uk.gov.hmcts.opal.entity.PrisonEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class LegacyPrisonServiceTest extends LegacyTestsBase {

    @Mock
    private Logger log;

    @InjectMocks
    private LegacyPrisonService legacyPrisonService;

    @Test
    void testGetPrison() {
        // Arrange

        PrisonEntity prisonEntity = PrisonEntity.builder().build();

        // Act
        LegacyGatewayResponseException exception = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyPrisonService.getPrison(1)
        );

        // Assert
        assertNotNull(legacyPrisonService.getLog());
        assertNotNull(exception);
        assertEquals(NOT_YET_IMPLEMENTED, exception.getMessage());

    }

    @Test
    void testSearchPrisons() {
        // Arrange
        PrisonSearchDto criteria = PrisonSearchDto.builder().build();

        // Act
        LegacyGatewayResponseException exception = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyPrisonService.searchPrisons(criteria)
        );

        // Assert
        assertNotNull(exception);
        assertEquals(NOT_YET_IMPLEMENTED, exception.getMessage());

    }

}
