package uk.gov.hmcts.opal.controllers.develop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.controllers.develop.PaymentTermsController;
import uk.gov.hmcts.opal.dto.search.PaymentTermsSearchDto;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.service.opal.PaymentTermsService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentTermsControllerTest {

    @Mock
    private PaymentTermsService paymentTermsService;

    @InjectMocks
    private PaymentTermsController paymentTermsController;

    @Test
    void testGetPaymentTerms_Success() {
        // Arrange
        PaymentTermsEntity entity = PaymentTermsEntity.builder().build();

        when(paymentTermsService.getPaymentTerms(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<PaymentTermsEntity> response = paymentTermsController.getPaymentTermsById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(paymentTermsService, times(1)).getPaymentTerms(any(Long.class));
    }

    @Test
    void testSearchPaymentTermss_Success() {
        // Arrange
        PaymentTermsEntity entity = PaymentTermsEntity.builder().build();
        List<PaymentTermsEntity> paymentTermsList = List.of(entity);

        when(paymentTermsService.searchPaymentTerms(any())).thenReturn(paymentTermsList);

        // Act
        PaymentTermsSearchDto searchDto = PaymentTermsSearchDto.builder().build();
        ResponseEntity<List<PaymentTermsEntity>> response = paymentTermsController.postPaymentTermsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(paymentTermsList, response.getBody());
        verify(paymentTermsService, times(1)).searchPaymentTerms(any());
    }

}
