package uk.gov.hmcts.opal.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchResultsDto;
import uk.gov.hmcts.opal.dto.AddNoteDto;
import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.dto.search.NoteSearchDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.opal.DefendantAccountService;
import uk.gov.hmcts.opal.service.opal.NoteService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.Collections;
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

    @Mock
    private UserStateService userStateService;

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
        UserState userState = UserState.builder()
            .userId("JS001").userName("John Smith").roles(Collections.emptySet()).build();

        when(noteService.saveNote(any(NoteDto.class))).thenReturn(mockResponse);
        when(userStateService.getUserStateUsingServletRequest(any())).thenReturn(userState);

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
        UserState userState = UserState.builder()
            .userId("JS001").userName("John Smith").roles(Collections.emptySet()).build();
        when(noteService.saveNote(any(NoteDto.class))).thenReturn(null);
        when(userStateService.getUserStateUsingServletRequest(any())).thenReturn(userState);

        // Act
        AddNoteDto addNote = AddNoteDto.builder().build();
        ResponseEntity<NoteDto> responseEntity = defendantAccountController.addNote(addNote, request);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(noteService, times(1)).saveNote(any(
            NoteDto.class));
    }

    @Test
    public void testNotes_Success() {
        // Arrange
        NoteDto mockNote = new NoteDto();
        List<NoteDto> mockResponse = List.of(mockNote);

        when(noteService.searchNotes(any(NoteSearchDto.class))).thenReturn(mockResponse);

        // Act
        AddNoteDto addNote = AddNoteDto.builder().build();
        ResponseEntity<List<NoteDto>> responseEntity = defendantAccountController.getNotesForDefendantAccount("1");

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
        verify(noteService, times(1)).searchNotes(any(
            NoteSearchDto.class));

    }

    @Test
    public void testNotes_NoContent() {
        when(noteService.searchNotes(any(NoteSearchDto.class))).thenReturn(null);

        // Act
        AddNoteDto addNote = AddNoteDto.builder().build();
        ResponseEntity<List<NoteDto>> responseEntity = defendantAccountController.getNotesForDefendantAccount("1");

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(noteService, times(1)).searchNotes(any(
            NoteSearchDto.class));
    }
}
