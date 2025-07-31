package uk.gov.hmcts.opal.disco.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.BacsPaymentSearchDto;
import uk.gov.hmcts.opal.entity.BacsPaymentEntity;
import uk.gov.hmcts.opal.disco.opal.BacsPaymentService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BacsPaymentControllerTest {

    @Mock
    private BacsPaymentService bacsPaymentService;

    @InjectMocks
    private BacsPaymentController bacsPaymentController;

    @Test
    void testGetBacsPayment_Success() {
        // Arrange
        BacsPaymentEntity entity = BacsPaymentEntity.builder().build();

        when(bacsPaymentService.getBacsPayment(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<BacsPaymentEntity> response = bacsPaymentController.getBacsPaymentById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(bacsPaymentService, times(1)).getBacsPayment(any(Long.class));
    }

    @Test
    void testSearchBacsPayments_Success() {
        // Arrange
        BacsPaymentEntity entity = BacsPaymentEntity.builder().build();
        List<BacsPaymentEntity> bacsPaymentList = List.of(entity);

        when(bacsPaymentService.searchBacsPayments(any())).thenReturn(bacsPaymentList);

        // Act
        BacsPaymentSearchDto searchDto = BacsPaymentSearchDto.builder().build();
        ResponseEntity<List<BacsPaymentEntity>> response = bacsPaymentController.postBacsPaymentsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(bacsPaymentList, response.getBody());
        verify(bacsPaymentService, times(1)).searchBacsPayments(any());
    }

}
