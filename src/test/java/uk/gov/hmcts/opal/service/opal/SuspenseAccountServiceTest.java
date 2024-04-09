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
import uk.gov.hmcts.opal.dto.search.SuspenseAccountSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseAccountEntity;
import uk.gov.hmcts.opal.repository.SuspenseAccountRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuspenseAccountServiceTest {

    @Mock
    private SuspenseAccountRepository suspenseAccountRepository;

    @InjectMocks
    private SuspenseAccountService suspenseAccountService;

    @Test
    void testGetSuspenseAccount() {
        // Arrange

        SuspenseAccountEntity suspenseAccountEntity = SuspenseAccountEntity.builder().build();
        when(suspenseAccountRepository.getReferenceById(any())).thenReturn(suspenseAccountEntity);

        // Act
        SuspenseAccountEntity result = suspenseAccountService.getSuspenseAccount(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchSuspenseAccounts() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);

        SuspenseAccountEntity suspenseAccountEntity = SuspenseAccountEntity.builder().build();
        Page<SuspenseAccountEntity> mockPage = new PageImpl<>(List.of(suspenseAccountEntity), Pageable.unpaged(), 999L);
        when(suspenseAccountRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<SuspenseAccountEntity> result = suspenseAccountService.searchSuspenseAccounts(
            SuspenseAccountSearchDto.builder().build());

        // Assert
        assertEquals(List.of(suspenseAccountEntity), result);

    }


}
