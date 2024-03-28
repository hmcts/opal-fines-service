package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import uk.gov.hmcts.opal.dto.search.MisDebtorSearchDto;
import uk.gov.hmcts.opal.entity.MisDebtorEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class LegacyMisDebtorServiceTest extends LegacyTestsBase {

    @Mock
    private Logger log;

    @InjectMocks
    private LegacyMisDebtorService legacyMisDebtorService;

    @Test
    void testGetMisDebtor() {
        // Arrange

        MisDebtorEntity misDebtorEntity = MisDebtorEntity.builder().build();

        // Act
        LegacyGatewayResponseException exception = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyMisDebtorService.getMisDebtor(1)
        );

        // Assert
        assertNotNull(legacyMisDebtorService.getLog());
        assertNotNull(exception);
        assertEquals(NOT_YET_IMPLEMENTED, exception.getMessage());

    }

    @Test
    void testSearchMisDebtors() {
        // Arrange
        MisDebtorSearchDto criteria = MisDebtorSearchDto.builder().build();

        // Act
        LegacyGatewayResponseException exception = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyMisDebtorService.searchMisDebtors(criteria)
        );

        // Assert
        assertNotNull(exception);
        assertEquals(NOT_YET_IMPLEMENTED, exception.getMessage());

    }

}
