package uk.gov.hmcts.opal.service.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.opal.dto.search.PaymentInSearchDto;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.repository.PaymentInRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentInServiceTest {

    @Mock
    private PaymentInRepository paymentInRepository;

    @InjectMocks
    private PaymentInService paymentInService;

    @Test
    void testGetPaymentIn() {
        // Arrange

        PaymentInEntity paymentInEntity = PaymentInEntity.builder().build();
        when(paymentInRepository.getReferenceById(any())).thenReturn(paymentInEntity);

        // Act
        PaymentInEntity result = paymentInService.getPaymentIn(1);

        // Assert
        assertNotNull(result);

    }

    @Test
    void testSearchPaymentIns() {
        // Arrange

        PaymentInEntity paymentInEntity = PaymentInEntity.builder().build();
        Page<PaymentInEntity> mockPage = new PageImpl<>(List.of(paymentInEntity), Pageable.unpaged(), 999L);
        // when(paymentInRepository.findBy(any(Specification.class), any())).thenReturn(mockPage);

        // Act
        List<PaymentInEntity> result = paymentInService.searchPaymentIns(PaymentInSearchDto.builder().build());

        // Assert
        assertNull(result);

    }


}
