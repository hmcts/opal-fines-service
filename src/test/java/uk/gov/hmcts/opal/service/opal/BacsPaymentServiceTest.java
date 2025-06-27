package uk.gov.hmcts.opal.service.opal;

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
import uk.gov.hmcts.opal.dto.search.BacsPaymentSearchDto;
import uk.gov.hmcts.opal.entity.BacsPaymentEntity;
import uk.gov.hmcts.opal.repository.BacsPaymentRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BacsPaymentServiceTest {

    @Mock
    private BacsPaymentRepository bacsPaymentRepository;

    @InjectMocks
    private BacsPaymentService bacsPaymentService;

    @Test
    void testGetBacsPayment() {
        // Arrange

        BacsPaymentEntity bacsPaymentEntity = BacsPaymentEntity.builder().build();
        when(bacsPaymentRepository.getReferenceById(any())).thenReturn(bacsPaymentEntity);

        // Act
        BacsPaymentEntity result = bacsPaymentService.getBacsPayment(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchBacsPayments() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);

        BacsPaymentEntity bacsPaymentEntity = BacsPaymentEntity.builder().build();
        Page<BacsPaymentEntity> mockPage = new PageImpl<>(List.of(bacsPaymentEntity), Pageable.unpaged(), 999L);
        when(bacsPaymentRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<BacsPaymentEntity> result = bacsPaymentService.searchBacsPayments(BacsPaymentSearchDto.builder().build());

        // Assert
        assertEquals(List.of(bacsPaymentEntity), result);

    }


}
