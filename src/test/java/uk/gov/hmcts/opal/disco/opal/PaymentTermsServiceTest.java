package uk.gov.hmcts.opal.disco.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor.SpecificationFluentQuery;
import uk.gov.hmcts.opal.dto.search.PaymentTermsSearchDto;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.repository.PaymentTermsRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @SuppressWarnings("unchecked")
    @Test
    void testSearchPaymentTermss() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);

        PaymentTermsEntity paymentTermsEntity = PaymentTermsEntity.builder().build();
        Page<PaymentTermsEntity> mockPage = new PageImpl<>(List.of(paymentTermsEntity), Pageable.unpaged(), 999L);
        when(paymentTermsRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<PaymentTermsEntity> result = paymentTermsService
            .searchPaymentTerms(PaymentTermsSearchDto.builder().build());

        // Assert
        assertEquals(List.of(paymentTermsEntity), result);

    }


}
