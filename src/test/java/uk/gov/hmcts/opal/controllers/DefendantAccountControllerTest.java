package uk.gov.hmcts.opal.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.AccountSearchDto;
import uk.gov.hmcts.opal.dto.AccountSearchResultsDto;
import uk.gov.hmcts.opal.dto.AddNoteDto;
import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.opal.DefendantAccountService;
import uk.gov.hmcts.opal.service.opal.NoteService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefendantAccountControllerTest {

    @Mock
    private DefendantAccountService defendantAccountService;

    @Mock
    private NoteService noteService;

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
    public void testGetDefendantAccountDetails_Success() {
        // Arrange
        AccountDetailsDto mockResponse = new AccountDetailsDto();

        when(defendantAccountService.getAccountDetailsByDefendantAccountId(any(Long.class)))
            .thenReturn(mockResponse);

        // Act
        ResponseEntity<AccountDetailsDto> responseEntity = defendantAccountController
            .getAccountDetailsByAccountSummary(1L);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
        verify(defendantAccountService, times(1)).getAccountDetailsByDefendantAccountId(any(
            Long.class));
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

    @Test
    public void testAddNote_Success() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        NoteDto mockResponse = new NoteDto();

        when(request.getRemoteUser()).thenReturn("REMOTE_USER");
        when(noteService.saveNote(any(NoteDto.class))).thenReturn(mockResponse);

        // Act
        AddNoteDto addNote = AddNoteDto.builder().build();
        ResponseEntity<NoteDto> responseEntity = defendantAccountController.addNote(addNote, request);

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
        verify(noteService, times(1)).saveNote(any(
            NoteDto.class));

    }

    @Test
    public void testAddNote_NoContent() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(noteService.saveNote(any(NoteDto.class))).thenReturn(null);
        when(request.getRemoteUser()).thenReturn("REMOTE_USER");

        // Act
        AddNoteDto addNote = AddNoteDto.builder().build();
        ResponseEntity<NoteDto> responseEntity = defendantAccountController.addNote(addNote, request);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(noteService, times(1)).saveNote(any(
            NoteDto.class));
    }
}
