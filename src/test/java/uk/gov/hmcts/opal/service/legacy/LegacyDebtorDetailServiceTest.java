package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import uk.gov.hmcts.opal.dto.search.DebtorDetailSearchDto;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class LegacyDebtorDetailServiceTest extends LegacyTestsBase {

    @Mock
    private Logger log;

    @InjectMocks
    private LegacyDebtorDetailService legacyDebtorDetailService;

    @Test
    void testGetDebtorDetail() {
        // Arrange

        DebtorDetailEntity debtorDetailEntity = DebtorDetailEntity.builder().build();

        // Act
        LegacyGatewayResponseException exception = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyDebtorDetailService.getDebtorDetail(1)
        );

        // Assert
        assertNotNull(legacyDebtorDetailService.getLog());
        assertNotNull(exception);
        assertEquals(NOT_YET_IMPLEMENTED, exception.getMessage());

    }

    @Test
    void testSearchDebtorDetails() {
        // Arrange

        DebtorDetailEntity debtorDetailEntity = DebtorDetailEntity.builder().build();

        // Act
        LegacyGatewayResponseException exception = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyDebtorDetailService.searchDebtorDetails(DebtorDetailSearchDto.builder().build())
        );

        // Assert
        assertNotNull(exception);
        assertEquals(NOT_YET_IMPLEMENTED, exception.getMessage());

    }

}
