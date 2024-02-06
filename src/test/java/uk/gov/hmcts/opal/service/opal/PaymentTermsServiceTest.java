package uk.gov.hmcts.opal.service.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.opal.dto.search.PaymentTermsSearchDto;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.repository.PaymentTermsRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentTermsServiceTest {

    @Mock
    private PaymentTermsRepository paymentTermsRepository;

    @InjectMocks
    private PaymentTermsService paymentTermsService;

    @Test
    void testGetPaymentTerms() {
        // Arrange

        PaymentTermsEntity paymentTermsEntity = PaymentTermsEntity.builder().build();
        when(paymentTermsRepository.getReferenceById(any())).thenReturn(paymentTermsEntity);

        // Act
        PaymentTermsEntity result = paymentTermsService.getPaymentTerms(1);

        // Assert
        assertNotNull(result);

    }

    @Test
    void testSearchPaymentTermss() {
        // Arrange

        PaymentTermsEntity paymentTermsEntity = PaymentTermsEntity.builder().build();
        Page<PaymentTermsEntity> mockPage = new PageImpl<>(List.of(paymentTermsEntity), Pageable.unpaged(), 999L);
        // when(paymentTermsRepository.findBy(any(Specification.class), any())).thenReturn(mockPage);

        // Act
        List<PaymentTermsEntity> result = paymentTermsService
            .searchPaymentTerms(PaymentTermsSearchDto.builder().build());

        // Assert
        assertNull(result);

    }


}
