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
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.repository.DraftAccountRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftAccountServiceTest {

    @Mock
    private DraftAccountRepository draftAccountRepository;

    @InjectMocks
    private DraftAccountService draftAccountService;

    @Test
    void testGetDraftAccount() {
        // Arrange

        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder().build();
        when(draftAccountRepository.getReferenceById(any())).thenReturn(draftAccountEntity);

        // Act
        DraftAccountEntity result = draftAccountService.getDraftAccount(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchDraftAccounts() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);

        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder().build();
        Page<DraftAccountEntity> mockPage = new PageImpl<>(List.of(draftAccountEntity), Pageable.unpaged(), 999L);
        when(draftAccountRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<DraftAccountEntity> result = draftAccountService.searchDraftAccounts(
            DraftAccountSearchDto.builder().build());

        // Assert
        assertEquals(List.of(draftAccountEntity), result);

    }

}
