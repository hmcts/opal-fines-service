package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.AccountSearchDto;
import uk.gov.hmcts.opal.dto.AccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.DefendantAccountService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class DefendantAccountControllerTest {

    @Mock
    private DefendantAccountService defendantAccountService;

    @InjectMocks
    private DefendantAccountController defendantAccountController;

    @Test
    public void testGetDefendantAccount_Success() {
        // Arrange
        DefendantAccountEntity mockResponse = new DefendantAccountEntity();

        when(defendantAccountService.getDefendantAccount(any(AccountEnquiryDto.class))).thenReturn(mockResponse);

        // Act
        ResponseEntity<DefendantAccountEntity> responseEntity = defendantAccountController.getDefendantAccount(
            (short)1, "");

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
        verify(defendantAccountService, times(1)).getDefendantAccount(any(
            AccountEnquiryDto.class));
    }

    @Test
    public void testGetDefendantAccount_NoContent() {

        when(defendantAccountService.getDefendantAccount(any(AccountEnquiryDto.class))).thenReturn(null);

        // Act
        ResponseEntity<DefendantAccountEntity> responseEntity = defendantAccountController.getDefendantAccount(
            (short)1, "");

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(defendantAccountService, times(1)).getDefendantAccount(any(
            AccountEnquiryDto.class));
    }

    @Test
    public void testPutDefendantAccount_Success() {
        // Arrange
        DefendantAccountEntity requestEntity = new DefendantAccountEntity();
        DefendantAccountEntity mockResponse = new DefendantAccountEntity();

        when(defendantAccountService.putDefendantAccount(any(DefendantAccountEntity.class))).thenReturn(mockResponse);

        // Act
        ResponseEntity<DefendantAccountEntity> responseEntity = defendantAccountController.putDefendantAccount(
            requestEntity);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
        verify(defendantAccountService, times(1)).putDefendantAccount(any(
            DefendantAccountEntity.class));
    }

    @Test
    public void testGetDefendantAccounts_Success() {
        // Arrange
        List<DefendantAccountEntity> mockResponse = List.of(new DefendantAccountEntity());

        when(defendantAccountService.getDefendantAccountsByBusinessUnit(any(Short.class))).thenReturn(mockResponse);

        // Act
        ResponseEntity<List<DefendantAccountEntity>> responseEntity = defendantAccountController
            .getDefendantAccountsByBusinessUnit(any(Short.class));

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
        verify(defendantAccountService, times(1)).getDefendantAccountsByBusinessUnit(any(
            Short.class));

    }

    @Test
    public void testPostDefendantAccountSearch_Success() {
        // Arrange
        AccountSearchDto requestEntity = AccountSearchDto.builder().build();
        AccountSearchResultsDto mockResponse = AccountSearchResultsDto.builder().build();

        when(defendantAccountService.searchDefendantAccounts(any(AccountSearchDto.class))).thenReturn(mockResponse);

        // Act
        ResponseEntity<AccountSearchResultsDto> responseEntity = defendantAccountController.postDefendantAccountSearch(
            requestEntity);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
        verify(defendantAccountService, times(1)).searchDefendantAccounts(any(
            AccountSearchDto.class));
    }
}
