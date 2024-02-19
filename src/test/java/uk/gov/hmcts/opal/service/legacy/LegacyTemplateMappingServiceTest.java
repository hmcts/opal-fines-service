package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import uk.gov.hmcts.opal.dto.search.TemplateMappingSearchDto;
import uk.gov.hmcts.opal.entity.TemplateMappingEntity.MappingId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class LegacyTemplateMappingServiceTest extends LegacyTestsBase {

    @Mock
    private Logger log;

    @InjectMocks
    private LegacyTemplateMappingService legacyTemplateMappingService;

    @Test
    void testGetTemplateMapping() {
        // Arrange
        MappingId key = new MappingId(1L, 1L);

        // Act
        LegacyGatewayResponseException exception = assertThrows(
            LegacyGatewayResponseException.class, () -> legacyTemplateMappingService.getTemplateMapping(key)
        );

        // Assert
        assertNotNull(legacyTemplateMappingService.getLog());
        assertNotNull(exception);
        assertEquals(NOT_YET_IMPLEMENTED, exception.getMessage());

    }

    @Test
    void testSearchTemplateMappings() {
        // Arrange
        TemplateMappingSearchDto criteria = TemplateMappingSearchDto.builder().build();

        // Act
        LegacyGatewayResponseException exception = assertThrows(
            LegacyGatewayResponseException.class, () -> legacyTemplateMappingService.searchTemplateMappings(criteria)
        );

        // Assert
        assertNotNull(exception);
        assertEquals(NOT_YET_IMPLEMENTED, exception.getMessage());

    }

}
