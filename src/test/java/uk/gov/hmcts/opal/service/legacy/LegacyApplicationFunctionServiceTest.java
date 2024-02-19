package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import uk.gov.hmcts.opal.dto.search.ApplicationFunctionSearchDto;
import uk.gov.hmcts.opal.entity.ApplicationFunctionEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class LegacyApplicationFunctionServiceTest extends LegacyTestsBase {

    @Mock
    private Logger log;

    @InjectMocks
    private LegacyApplicationFunctionService legacyApplicationFunctionService;

    @Test
    void testGetApplicationFunction() {
        // Arrange

        ApplicationFunctionEntity applicationFunctionEntity = ApplicationFunctionEntity.builder().build();

        // Act
        LegacyGatewayResponseException exception = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyApplicationFunctionService.getApplicationFunction(1)
        );

        // Assert
        assertNotNull(legacyApplicationFunctionService.getLog());
        assertNotNull(exception);
        assertEquals(NOT_YET_IMPLEMENTED, exception.getMessage());

    }

    @Test
    void testSearchApplicationFunctions() {
        // Arrange
        ApplicationFunctionSearchDto criteria = ApplicationFunctionSearchDto.builder().build();

        // Act
        LegacyGatewayResponseException exception = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyApplicationFunctionService.searchApplicationFunctions(criteria)
        );

        // Assert
        assertNotNull(exception);
        assertEquals(NOT_YET_IMPLEMENTED, exception.getMessage());

    }

}
