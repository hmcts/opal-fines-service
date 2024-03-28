package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import uk.gov.hmcts.opal.dto.search.LogActionSearchDto;
import uk.gov.hmcts.opal.entity.LogActionEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class LegacyLogActionServiceTest extends LegacyTestsBase {

    @Mock
    private Logger log;

    @InjectMocks
    private LegacyLogActionService legacyLogActionService;

    @Test
    void testGetLogAction() {
        // Arrange

        LogActionEntity logActionEntity = LogActionEntity.builder().build();

        // Act
        LegacyGatewayResponseException exception = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyLogActionService.getLogAction((short)1)
        );

        // Assert
        assertNotNull(legacyLogActionService.getLog());
        assertNotNull(exception);
        assertEquals(NOT_YET_IMPLEMENTED, exception.getMessage());

    }

    @Test
    void testSearchLogActions() {
        // Arrange
        LogActionSearchDto criteria = LogActionSearchDto.builder().build();

        // Act
        LegacyGatewayResponseException exception = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyLogActionService.searchLogActions(criteria)
        );

        // Assert
        assertNotNull(exception);
        assertEquals(NOT_YET_IMPLEMENTED, exception.getMessage());

    }

}
