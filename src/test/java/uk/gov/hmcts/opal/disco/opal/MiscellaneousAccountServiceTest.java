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
import uk.gov.hmcts.opal.dto.search.MiscellaneousAccountSearchDto;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity;
import uk.gov.hmcts.opal.repository.MiscellaneousAccountRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MiscellaneousAccountServiceTest {

    @Mock
    private MiscellaneousAccountRepository miscellaneousAccountRepository;

    @InjectMocks
    private MiscellaneousAccountService miscellaneousAccountService;

    @Test
    void testGetMiscellaneousAccount() {
        // Arrange

        MiscellaneousAccountEntity miscellaneousAccountEntity = MiscellaneousAccountEntity.builder().build();
        when(miscellaneousAccountRepository.getReferenceById(any())).thenReturn(miscellaneousAccountEntity);

        // Act
        MiscellaneousAccountEntity result = miscellaneousAccountService.getMiscellaneousAccount(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchMiscellaneousAccounts() {
        // Arrange
        SpecificationFluentQuery sfq = Mockito.mock(SpecificationFluentQuery.class);

        MiscellaneousAccountEntity miscellaneousAccountEntity = MiscellaneousAccountEntity.builder().build();
        Page<MiscellaneousAccountEntity> mockPage = new PageImpl<>(List.of(miscellaneousAccountEntity),
                                                                   Pageable.unpaged(), 999L);
        when(miscellaneousAccountRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(sfq);
            return mockPage;
        });

        // Act
        List<MiscellaneousAccountEntity> result = miscellaneousAccountService
            .searchMiscellaneousAccounts(MiscellaneousAccountSearchDto.builder().build());

        // Assert
        assertEquals(List.of(miscellaneousAccountEntity), result);

    }


}
