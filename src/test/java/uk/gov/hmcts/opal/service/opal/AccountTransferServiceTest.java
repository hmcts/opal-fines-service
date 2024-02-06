package uk.gov.hmcts.opal.service.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.opal.dto.search.AccountTransferSearchDto;
import uk.gov.hmcts.opal.entity.AccountTransferEntity;
import uk.gov.hmcts.opal.repository.AccountTransferRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountTransferServiceTest {

    @Mock
    private AccountTransferRepository accountTransferRepository;

    @InjectMocks
    private AccountTransferService accountTransferService;

    @Test
    void testGetAccountTransfer() {
        // Arrange

        AccountTransferEntity accountTransferEntity = AccountTransferEntity.builder().build();
        when(accountTransferRepository.getReferenceById(any())).thenReturn(accountTransferEntity);

        // Act
        AccountTransferEntity result = accountTransferService.getAccountTransfer(1);

        // Assert
        assertNotNull(result);

    }

    @Test
    void testSearchAccountTransfers() {
        // Arrange

        AccountTransferEntity accountTransferEntity = AccountTransferEntity.builder().build();
        Page<AccountTransferEntity> mockPage = new PageImpl<>(List.of(accountTransferEntity),
                                                              Pageable.unpaged(), 999L);
        // when(accountTransferRepository.findBy(any(Specification.class), any())).thenReturn(mockPage);

        // Act
        List<AccountTransferEntity> result = accountTransferService
            .searchAccountTransfers(AccountTransferSearchDto.builder().build());

        // Assert
        assertNull(result);

    }


}
