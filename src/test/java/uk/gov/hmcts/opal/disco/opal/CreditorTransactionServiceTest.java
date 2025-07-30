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
import uk.gov.hmcts.opal.dto.search.CreditorTransactionSearchDto;
import uk.gov.hmcts.opal.entity.CreditorTransactionEntity;
import uk.gov.hmcts.opal.repository.CreditorTransactionRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditorTransactionServiceTest {

    @Mock
    private CreditorTransactionRepository creditorTransactionRepository;

    @InjectMocks
    private CreditorTransactionService creditorTransactionService;

    @Test
    void testGetCreditorTransaction() {
        // Arrange

        CreditorTransactionEntity creditorTransactionEntity = CreditorTransactionEntity.builder().build();
        when(creditorTransactionRepository.getReferenceById(any())).thenReturn(creditorTransactionEntity);

        // Act
        CreditorTransactionEntity result = creditorTransactionService.getCreditorTransaction(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchCreditorTransactions() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);

        CreditorTransactionEntity creditorTransactionEntity = CreditorTransactionEntity.builder().build();
        Page<CreditorTransactionEntity> mockPage = new PageImpl<>(List.of(creditorTransactionEntity),
                                                                  Pageable.unpaged(), 999L);
        when(creditorTransactionRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<CreditorTransactionEntity> result = creditorTransactionService.searchCreditorTransactions(
            CreditorTransactionSearchDto.builder().build());

        // Assert
        assertEquals(List.of(creditorTransactionEntity), result);

    }


}
