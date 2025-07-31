package uk.gov.hmcts.opal.disco.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.CourtFeeSearchDto;
import uk.gov.hmcts.opal.entity.CourtFeeEntity;
import uk.gov.hmcts.opal.disco.opal.CourtFeeService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourtFeeControllerTest {

    @Mock
    private CourtFeeService courtFeeService;

    @InjectMocks
    private CourtFeeController courtFeeController;

    @Test
    void testGetCourtFee_Success() {
        // Arrange
        CourtFeeEntity entity = CourtFeeEntity.builder().build();

        when(courtFeeService.getCourtFee(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<CourtFeeEntity> response = courtFeeController.getCourtFeeById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(courtFeeService, times(1)).getCourtFee(any(Long.class));
    }

    @Test
    void testSearchCourtFees_Success() {
        // Arrange
        CourtFeeEntity entity = CourtFeeEntity.builder().build();
        List<CourtFeeEntity> courtFeeList = List.of(entity);

        when(courtFeeService.searchCourtFees(any())).thenReturn(courtFeeList);

        // Act
        CourtFeeSearchDto searchDto = CourtFeeSearchDto.builder().build();
        ResponseEntity<List<CourtFeeEntity>> response = courtFeeController.postCourtFeesSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(courtFeeList, response.getBody());
        verify(courtFeeService, times(1)).searchCourtFees(any());
    }

}
