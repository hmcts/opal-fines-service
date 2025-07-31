package uk.gov.hmcts.opal.disco.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.MiscellaneousAccountSearchDto;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity;
import uk.gov.hmcts.opal.disco.opal.MiscellaneousAccountService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MiscellaneousAccountControllerTest {

    @Mock
    private MiscellaneousAccountService miscellaneousAccountService;

    @InjectMocks
    private MiscellaneousAccountController miscellaneousAccountController;

    @Test
    void testGetMiscellaneousAccount_Success() {
        // Arrange
        MiscellaneousAccountEntity entity = MiscellaneousAccountEntity.builder().build();

        when(miscellaneousAccountService.getMiscellaneousAccount(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<MiscellaneousAccountEntity> response = miscellaneousAccountController
            .getMiscellaneousAccountById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(miscellaneousAccountService, times(1)).getMiscellaneousAccount(any(Long.class));
    }

    @Test
    void testSearchMiscellaneousAccounts_Success() {
        // Arrange
        MiscellaneousAccountEntity entity = MiscellaneousAccountEntity.builder().build();
        List<MiscellaneousAccountEntity> miscellaneousAccountList = List.of(entity);

        when(miscellaneousAccountService.searchMiscellaneousAccounts(any())).thenReturn(miscellaneousAccountList);

        // Act
        MiscellaneousAccountSearchDto searchDto = MiscellaneousAccountSearchDto.builder().build();
        ResponseEntity<List<MiscellaneousAccountEntity>> response = miscellaneousAccountController
            .postMiscellaneousAccountsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(miscellaneousAccountList, response.getBody());
        verify(miscellaneousAccountService, times(1)).searchMiscellaneousAccounts(any());
    }

}
