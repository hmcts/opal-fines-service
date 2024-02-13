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
import org.springframework.data.repository.query.FluentQuery;
import uk.gov.hmcts.opal.dto.search.DebtorDetailSearchDto;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.repository.DebtorDetailRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtorDetailServiceTest {

    @Mock
    private DebtorDetailRepository debtorDetailRepository;

    @InjectMocks
    private DebtorDetailService debtorDetailService;

    @Test
    void testGetDebtorDetail() {
        // Arrange

        DebtorDetailEntity debtorDetailEntity = DebtorDetailEntity.builder().build();
        when(debtorDetailRepository.getReferenceById(any())).thenReturn(debtorDetailEntity);

        // Act
        DebtorDetailEntity result = debtorDetailService.getDebtorDetail(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchDebtorDetails() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);

        DebtorDetailEntity debtorDetailEntity = DebtorDetailEntity.builder().build();
        Page<DebtorDetailEntity> mockPage = new PageImpl<>(List.of(debtorDetailEntity), Pageable.unpaged(), 999L);
        when(debtorDetailRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<DebtorDetailEntity> result = debtorDetailService
            .searchDebtorDetails(DebtorDetailSearchDto.builder().build());

        // Assert
        assertEquals(List.of(debtorDetailEntity), result);

    }


}
