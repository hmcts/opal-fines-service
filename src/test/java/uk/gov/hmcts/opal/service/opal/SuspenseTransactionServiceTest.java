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
import uk.gov.hmcts.opal.dto.search.SuspenseTransactionSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseTransactionEntity;
import uk.gov.hmcts.opal.repository.SuspenseTransactionRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuspenseTransactionServiceTest {

    @Mock
    private SuspenseTransactionRepository suspenseTransactionRepository;

    @InjectMocks
    private SuspenseTransactionService suspenseTransactionService;

    @Test
    void testGetSuspenseTransaction() {
        // Arrange

        SuspenseTransactionEntity suspenseTransactionEntity = SuspenseTransactionEntity.builder().build();
        when(suspenseTransactionRepository.getReferenceById(any())).thenReturn(suspenseTransactionEntity);

        // Act
        SuspenseTransactionEntity result = suspenseTransactionService.getSuspenseTransaction(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchSuspenseTransactions() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);

        SuspenseTransactionEntity suspenseTransactionEntity = SuspenseTransactionEntity.builder().build();
        Page<SuspenseTransactionEntity> mockPage = new PageImpl<>(List.of(suspenseTransactionEntity),
                                                                  Pageable.unpaged(), 999L);
        when(suspenseTransactionRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<SuspenseTransactionEntity> result = suspenseTransactionService.searchSuspenseTransactions(
            SuspenseTransactionSearchDto.builder().build());

        // Assert
        assertEquals(List.of(suspenseTransactionEntity), result);

    }


}
