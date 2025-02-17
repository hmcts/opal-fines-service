package uk.gov.hmcts.opal.controllers.develop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.FixedPenaltyOffenceSearchDto;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.service.opal.FixedPenaltyOffenceService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FixedPenaltyOffenceControllerTest {

    @Mock
    private FixedPenaltyOffenceService fixedPenaltyOffenceService;

    @InjectMocks
    private FixedPenaltyOffenceController fixedPenaltyOffenceController;

    @Test
    void testGetFixedPenaltyOffence_Success() {
        // Arrange
        FixedPenaltyOffenceEntity entity = FixedPenaltyOffenceEntity.builder().build();

        when(fixedPenaltyOffenceService.getFixedPenaltyOffence(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<FixedPenaltyOffenceEntity> response = fixedPenaltyOffenceController
            .getFixedPenaltyOffenceById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(fixedPenaltyOffenceService, times(1)).getFixedPenaltyOffence(any(Long.class));
    }

    @Test
    void testSearchFixedPenaltyOffences_Success() {
        // Arrange
        FixedPenaltyOffenceEntity entity = FixedPenaltyOffenceEntity.builder().build();
        List<FixedPenaltyOffenceEntity> fixedPenaltyOffenceList = List.of(entity);

        when(fixedPenaltyOffenceService.searchFixedPenaltyOffences(any())).thenReturn(fixedPenaltyOffenceList);

        // Act
        FixedPenaltyOffenceSearchDto searchDto = FixedPenaltyOffenceSearchDto.builder().build();
        ResponseEntity<List<FixedPenaltyOffenceEntity>> response = fixedPenaltyOffenceController
            .postFixedPenaltyOffencesSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(fixedPenaltyOffenceList, response.getBody());
        verify(fixedPenaltyOffenceService, times(1)).searchFixedPenaltyOffences(any());
    }

}
