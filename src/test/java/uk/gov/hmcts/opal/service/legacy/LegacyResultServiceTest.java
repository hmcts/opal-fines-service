package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;
import uk.gov.hmcts.opal.entity.ResultEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class LegacyResultServiceTest extends LegacyTestsBase {

    @Mock
    private Logger log;

    @InjectMocks
    private LegacyResultService legacyResultService;

    @Test
    void testGetResult() {
        // Arrange

        ResultEntity resultEntity = ResultEntity.builder().build();

        // Act
        LegacyGatewayResponseException exception = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyResultService.getResult(1)
        );

        // Assert
        assertNotNull(legacyResultService.getLog());
        assertNotNull(exception);
        assertEquals(NOT_YET_IMPLEMENTED, exception.getMessage());

    }

    @Test
    void testSearchResults() {
        // Arrange

        ResultSearchDto criteria = ResultSearchDto.builder().build();
        // Act
        LegacyGatewayResponseException exception = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyResultService.searchResults(criteria)
        );

        // Assert
        assertNotNull(exception);
        assertEquals(NOT_YET_IMPLEMENTED, exception.getMessage());

    }

}
