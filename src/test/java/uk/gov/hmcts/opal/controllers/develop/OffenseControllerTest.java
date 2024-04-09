package uk.gov.hmcts.opal.controllers.develop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.OffenseSearchDto;
import uk.gov.hmcts.opal.entity.OffenseEntity;
import uk.gov.hmcts.opal.service.opal.OffenseService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OffenseControllerTest {

    @Mock
    private OffenseService offenseService;

    @InjectMocks
    private OffenseController offenseController;

    @Test
    void testGetOffense_Success() {
        // Arrange
        OffenseEntity entity = OffenseEntity.builder().build();

        when(offenseService.getOffense(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<OffenseEntity> response = offenseController.getOffenseById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(offenseService, times(1)).getOffense(any(Long.class));
    }

    @Test
    void testSearchOffenses_Success() {
        // Arrange
        OffenseEntity entity = OffenseEntity.builder().build();
        List<OffenseEntity> offenseList = List.of(entity);

        when(offenseService.searchOffenses(any())).thenReturn(offenseList);

        // Act
        OffenseSearchDto searchDto = OffenseSearchDto.builder().build();
        ResponseEntity<List<OffenseEntity>> response = offenseController.postOffensesSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(offenseList, response.getBody());
        verify(offenseService, times(1)).searchOffenses(any());
    }

}
