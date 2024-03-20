package uk.gov.hmcts.opal.controllers.develop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.controllers.develop.EnforcementController;
import uk.gov.hmcts.opal.dto.search.EnforcementSearchDto;
import uk.gov.hmcts.opal.entity.EnforcementEntity;
import uk.gov.hmcts.opal.service.opal.EnforcementService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnforcementControllerTest {

    @Mock
    private EnforcementService enforcementService;

    @InjectMocks
    private EnforcementController enforcementController;

    @Test
    void testGetEnforcement_Success() {
        // Arrange
        EnforcementEntity entity = EnforcementEntity.builder().build();

        when(enforcementService.getEnforcement(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<EnforcementEntity> response = enforcementController.getEnforcementById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(enforcementService, times(1)).getEnforcement(any(Long.class));
    }

    @Test
    void testSearchEnforcements_Success() {
        // Arrange
        EnforcementEntity entity = EnforcementEntity.builder().build();
        List<EnforcementEntity> enforcementList = List.of(entity);

        when(enforcementService.searchEnforcements(any())).thenReturn(enforcementList);

        // Act
        EnforcementSearchDto searchDto = EnforcementSearchDto.builder().build();
        ResponseEntity<List<EnforcementEntity>> response = enforcementController.postEnforcementsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(enforcementList, response.getBody());
        verify(enforcementService, times(1)).searchEnforcements(any());
    }

}
