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
import uk.gov.hmcts.opal.dto.search.CourtFeeSearchDto;
import uk.gov.hmcts.opal.entity.CourtFeeEntity;
import uk.gov.hmcts.opal.repository.CourtFeeRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourtFeeServiceTest {

    @Mock
    private CourtFeeRepository courtFeeRepository;

    @InjectMocks
    private CourtFeeService courtFeeService;

    @Test
    void testGetCourtFee() {
        // Arrange

        CourtFeeEntity courtFeeEntity = CourtFeeEntity.builder().build();
        when(courtFeeRepository.getReferenceById(any())).thenReturn(courtFeeEntity);

        // Act
        CourtFeeEntity result = courtFeeService.getCourtFee(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchCourtFees() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);

        CourtFeeEntity courtFeeEntity = CourtFeeEntity.builder().build();
        Page<CourtFeeEntity> mockPage = new PageImpl<>(List.of(courtFeeEntity), Pageable.unpaged(), 999L);
        when(courtFeeRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<CourtFeeEntity> result = courtFeeService.searchCourtFees(CourtFeeSearchDto.builder().build());

        // Assert
        assertEquals(List.of(courtFeeEntity), result);

    }


}
