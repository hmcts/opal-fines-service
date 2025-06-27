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
import uk.gov.hmcts.opal.dto.search.ChequeSearchDto;
import uk.gov.hmcts.opal.entity.ChequeEntity;
import uk.gov.hmcts.opal.repository.ChequeRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChequeServiceTest {

    @Mock
    private ChequeRepository chequeRepository;

    @InjectMocks
    private ChequeService chequeService;

    @Test
    void testGetCheque() {
        // Arrange

        ChequeEntity chequeEntity = ChequeEntity.builder().build();
        when(chequeRepository.getReferenceById(any())).thenReturn(chequeEntity);

        // Act
        ChequeEntity result = chequeService.getCheque(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchCheques() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);

        ChequeEntity chequeEntity = ChequeEntity.builder().build();
        Page<ChequeEntity> mockPage = new PageImpl<>(List.of(chequeEntity), Pageable.unpaged(), 999L);
        when(chequeRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<ChequeEntity> result = chequeService.searchCheques(ChequeSearchDto.builder().build());

        // Assert
        assertEquals(List.of(chequeEntity), result);

    }


}
