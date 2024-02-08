package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.DebtorDetailSearchDto;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.service.opal.DebtorDetailService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtorDetailControllerTest {

    @Mock
    private DebtorDetailService debtorDetailService;

    @InjectMocks
    private DebtorDetailController debtorDetailController;

    @Test
    void testGetDebtorDetail_Success() {
        // Arrange
        DebtorDetailEntity entity = DebtorDetailEntity.builder().build(); //some id assigned by db sequence

        when(debtorDetailService.getDebtorDetail(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<DebtorDetailEntity> response = debtorDetailController.getDebtorDetailById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(debtorDetailService, times(1)).getDebtorDetail(any(Long.class));
    }

    @Test
    void testSearchDebtorDetails_Success() {
        // Arrange
        DebtorDetailEntity entity = DebtorDetailEntity.builder().build();
        List<DebtorDetailEntity> debtorDetailList = List.of(entity);

        when(debtorDetailService.searchDebtorDetails(any())).thenReturn(debtorDetailList);

        // Act
        DebtorDetailSearchDto searchDto = DebtorDetailSearchDto.builder().build();
        ResponseEntity<List<DebtorDetailEntity>> response = debtorDetailController.postDebtorDetailsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(debtorDetailList, response.getBody());
        verify(debtorDetailService, times(1)).searchDebtorDetails(any());
    }

}
