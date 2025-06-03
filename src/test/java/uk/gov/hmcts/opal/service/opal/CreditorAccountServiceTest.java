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
import uk.gov.hmcts.opal.dto.search.CreditorAccountSearchDto;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditorAccountServiceTest {

    @Mock
    private CreditorAccountRepository creditorAccountRepository;

    @InjectMocks
    private CreditorAccountService creditorAccountService;

    @Test
    void testGetCreditorAccount() {
        // Arrange

        CreditorAccountEntity creditorAccountEntity = CreditorAccountEntity.builder().build();
        when(creditorAccountRepository.getReferenceById(any())).thenReturn(creditorAccountEntity);

        // Act
        CreditorAccountEntity result = creditorAccountService.getCreditorAccount(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchCreditorAccounts() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);

        CreditorAccountEntity creditorAccountEntity = CreditorAccountEntity.builder().build();
        Page<CreditorAccountEntity> mockPage = new PageImpl<>(List.of(creditorAccountEntity),
                                                              Pageable.unpaged(), 999L);
        when(creditorAccountRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<CreditorAccountEntity> result = creditorAccountService.searchCreditorAccounts(
            CreditorAccountSearchDto.builder().build());

        // Assert
        assertEquals(List.of(creditorAccountEntity), result);

    }


}
