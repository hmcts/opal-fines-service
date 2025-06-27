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
import uk.gov.hmcts.opal.dto.search.DefendantTransactionSearchDto;
import uk.gov.hmcts.opal.entity.DefendantTransactionEntity;
import uk.gov.hmcts.opal.repository.DefendantTransactionRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefendantTransactionServiceTest {

    @Mock
    private DefendantTransactionRepository defendantTransactionRepository;

    @InjectMocks
    private DefendantTransactionService defendantTransactionService;

    @Test
    void testGetDefendantTransaction() {
        // Arrange

        DefendantTransactionEntity defendantTransactionEntity = DefendantTransactionEntity.builder().build();
        when(defendantTransactionRepository.getReferenceById(any())).thenReturn(defendantTransactionEntity);

        // Act
        DefendantTransactionEntity result = defendantTransactionService.getDefendantTransaction(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchDefendantTransactions() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);

        DefendantTransactionEntity defendantTransactionEntity = DefendantTransactionEntity.builder().build();
        Page<DefendantTransactionEntity> mockPage = new PageImpl<>(List.of(defendantTransactionEntity),
                                                                   Pageable.unpaged(), 999L);
        when(defendantTransactionRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<DefendantTransactionEntity> result = defendantTransactionService
            .searchDefendantTransactions(DefendantTransactionSearchDto.builder().build());

        // Assert
        assertEquals(List.of(defendantTransactionEntity), result);

    }


}
