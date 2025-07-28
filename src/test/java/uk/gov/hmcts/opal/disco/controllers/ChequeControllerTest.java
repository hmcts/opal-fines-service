package uk.gov.hmcts.opal.disco.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.ChequeSearchDto;
import uk.gov.hmcts.opal.entity.ChequeEntity;
import uk.gov.hmcts.opal.disco.opal.ChequeService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChequeControllerTest {

    @Mock
    private ChequeService chequeService;

    @InjectMocks
    private ChequeController chequeController;

    @Test
    void testGetCheque_Success() {
        // Arrange
        ChequeEntity entity = ChequeEntity.builder().build();

        when(chequeService.getCheque(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<ChequeEntity> response = chequeController.getChequeById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(chequeService, times(1)).getCheque(any(Long.class));
    }

    @Test
    void testSearchCheques_Success() {
        // Arrange
        ChequeEntity entity = ChequeEntity.builder().build();
        List<ChequeEntity> chequeList = List.of(entity);

        when(chequeService.searchCheques(any())).thenReturn(chequeList);

        // Act
        ChequeSearchDto searchDto = ChequeSearchDto.builder().build();
        ResponseEntity<List<ChequeEntity>> response = chequeController.postChequesSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(chequeList, response.getBody());
        verify(chequeService, times(1)).searchCheques(any());
    }

}
