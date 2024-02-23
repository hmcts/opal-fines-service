package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.PaymentInSearchDto;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.service.opal.PaymentInService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentInControllerTest {

    @Mock
    private PaymentInService paymentInService;

    @InjectMocks
    private PaymentInController paymentInController;

    @Test
    void testGetPaymentIn_Success() {
        // Arrange
        PaymentInEntity entity = PaymentInEntity.builder().build();

        when(paymentInService.getPaymentIn(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<PaymentInEntity> response = paymentInController.getPaymentInById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(paymentInService, times(1)).getPaymentIn(any(Long.class));
    }

    @Test
    void testSearchPaymentIns_Success() {
        // Arrange
        PaymentInEntity entity = PaymentInEntity.builder().build();
        List<PaymentInEntity> paymentInList = List.of(entity);

        when(paymentInService.searchPaymentIns(any())).thenReturn(paymentInList);

        // Act
        PaymentInSearchDto searchDto = PaymentInSearchDto.builder().build();
        ResponseEntity<List<PaymentInEntity>> response = paymentInController.postPaymentInsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(paymentInList, response.getBody());
        verify(paymentInService, times(1)).searchPaymentIns(any());
    }

}
