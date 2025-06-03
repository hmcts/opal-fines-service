package uk.gov.hmcts.opal.controllers.develop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.search.CreditorAccountSearchDto;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.service.opal.CreditorAccountService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditorAccountControllerTest {

    @Mock
    private CreditorAccountService creditorAccountService;

    @InjectMocks
    private CreditorAccountController creditorAccountController;

    @Test
    void testGetCreditorAccount_Success() {
        // Arrange
        CreditorAccountEntity entity = CreditorAccountEntity.builder().build();

        when(creditorAccountService.getCreditorAccount(any(Long.class))).thenReturn(entity);

        // Act
        ResponseEntity<CreditorAccountEntity> response = creditorAccountController.getCreditorAccountById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(entity, response.getBody());
        verify(creditorAccountService, times(1)).getCreditorAccount(any(Long.class));
    }

    @Test
    void testSearchCreditorAccounts_Success() {
        // Arrange
        CreditorAccountEntity entity = CreditorAccountEntity.builder().build();
        List<CreditorAccountEntity> creditorAccountList = List.of(entity);

        when(creditorAccountService.searchCreditorAccounts(any())).thenReturn(creditorAccountList);

        // Act
        CreditorAccountSearchDto searchDto = CreditorAccountSearchDto.builder().build();
        ResponseEntity<List<CreditorAccountEntity>> response = creditorAccountController
            .postCreditorAccountsSearch(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(creditorAccountList, response.getBody());
        verify(creditorAccountService, times(1)).searchCreditorAccounts(any());
    }

}
