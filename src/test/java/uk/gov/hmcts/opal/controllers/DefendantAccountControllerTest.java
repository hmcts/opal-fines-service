package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.disco.opal.DiscoDefendantAccountService;
import uk.gov.hmcts.opal.disco.opal.NoteService;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.AddNoteDto;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.dto.search.NoteSearchDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.DefendantAccountService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefendantAccountControllerTest {

    static final String BEARER_TOKEN = "Bearer a_token_here";

    static final String NOT_FOUND_JSON = """
        { "error": "Not Found", "message": "No resource found at provided URI"}""";

    @Mock
    private DefendantAccountService defendantAccountService;

    @Mock
    private DiscoDefendantAccountService discoDefendantAccountService;

    @Mock
    private NoteService noteService;

    @Mock
    private UserStateService userStateService;

    @InjectMocks
    private DefendantAccountController defendantAccountController;

    // TODO - This is Disco+ Code. To Be Removed?
    @Test
    void testGetDefendantAccount_Success() {
        // Arrange
        DefendantAccountEntity mockResponse = new DefendantAccountEntity();

        when(discoDefendantAccountService.getDefendantAccount(any(AccountEnquiryDto.class)))
            .thenReturn(mockResponse);

        // Act
        ResponseEntity<DefendantAccountEntity> responseEntity = defendantAccountController.getDefendantAccount(
            (short) 1, "", BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
        verify(discoDefendantAccountService, times(1)).getDefendantAccount(any(
            AccountEnquiryDto.class));
    }

    // TODO - This is Disco+ Code. To Be Removed?
    @Test
    void testGetDefendantAccount_NoContent() {
        // Arrange
        when(discoDefendantAccountService.getDefendantAccount(any(AccountEnquiryDto.class)))
            .thenReturn(null);

        // Act
        ResponseEntity<DefendantAccountEntity> responseEntity = defendantAccountController.getDefendantAccount(
            (short) 1, "", BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        verify(discoDefendantAccountService, times(1)).getDefendantAccount(any(
            AccountEnquiryDto.class));
    }

    // TODO - This is Disco+ Code. To Be Removed?
    @Test
    void testPutDefendantAccount_Success() {
        // Arrange
        DefendantAccountEntity requestEntity = new DefendantAccountEntity();
        DefendantAccountEntity mockResponse = new DefendantAccountEntity();

        when(discoDefendantAccountService.putDefendantAccount(any(DefendantAccountEntity.class)))
            .thenReturn(mockResponse);

        // Act
        ResponseEntity<DefendantAccountEntity> responseEntity = defendantAccountController.putDefendantAccount(
            requestEntity, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
        verify(discoDefendantAccountService, times(1)).putDefendantAccount(any(
            DefendantAccountEntity.class));
    }

    // TODO - This is Disco+ Code. To Be Removed?
    @Test
    void testGetDefendantAccountDetails_Success() {
        // Arrange
        AccountDetailsDto mockResponse = new AccountDetailsDto();

        when(discoDefendantAccountService.getAccountDetailsByDefendantAccountId(any(Long.class)))
            .thenReturn(mockResponse);

        // Act
        ResponseEntity<AccountDetailsDto> responseEntity = defendantAccountController
            .getAccountDetails(1L, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
        verify(discoDefendantAccountService, times(1))
            .getAccountDetailsByDefendantAccountId(any(
            Long.class));
    }

    // TODO - This is Disco+ Code. To Be Removed?
    @Test
    void testPostDefendantAccountSearch_Success() {
        // Arrange
        AccountSearchDto requestEntity = AccountSearchDto.builder().build();
        DefendantAccountSearchResultsDto mockResponse = DefendantAccountSearchResultsDto.builder().build();

        when(defendantAccountService.searchDefendantAccounts(any(AccountSearchDto.class), eq(BEARER_TOKEN)))
            .thenReturn(mockResponse);

        // Act
        ResponseEntity<DefendantAccountSearchResultsDto> responseEntity =
            defendantAccountController.postDefendantAccountSearch(requestEntity, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(mockResponse, responseEntity.getBody());

        verify(defendantAccountService, times(1))
            .searchDefendantAccounts(any(AccountSearchDto.class), eq(BEARER_TOKEN));
    }

    // TODO - This is Disco+ Code. To Be Removed?
    @Test
    void testAddNote_Success() {
        // Arrange
        NoteDto mockResponse = new NoteDto();
        UserState userState = UserStateUtil.permissionUser(
            (short)50, Permissions.ACCOUNT_ENQUIRY, Permissions.ACCOUNT_ENQUIRY_NOTES);

        when(noteService.saveNote(any(NoteDto.class))).thenReturn(mockResponse);
        when(userStateService.getUserStateUsingAuthToken(any())).thenReturn(userState);

        // Act
        AddNoteDto addNote = AddNoteDto.builder().businessUnitId((short) 50).build();
        ResponseEntity<NoteDto> responseEntity = defendantAccountController.addNote(addNote, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
        verify(noteService, times(1)).saveNote(any(
            NoteDto.class));

    }

    // TODO - This is Disco+ Code. To Be Removed?
    @Test
    void testAddNote_NoContent() {
        // Arrange
        UserState userState = UserStateUtil.permissionUser(
            (short)50, Permissions.ACCOUNT_ENQUIRY, Permissions.ACCOUNT_ENQUIRY_NOTES);

        when(noteService.saveNote(any(NoteDto.class))).thenReturn(null);
        when(userStateService.getUserStateUsingAuthToken(any())).thenReturn(userState);

        // Act
        AddNoteDto addNote = AddNoteDto.builder().businessUnitId((short) 50).build();
        ResponseEntity<NoteDto> responseEntity = defendantAccountController.addNote(addNote, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        verify(noteService, times(1)).saveNote(any(
            NoteDto.class));

    }

    // TODO - This is Disco+ Code. To Be Removed?
    @Test
    void testNotes_Success() {
        // Arrange
        NoteDto mockNote = new NoteDto();
        List<NoteDto> mockResponse = List.of(mockNote);

        when(noteService.searchNotes(any(NoteSearchDto.class))).thenReturn(mockResponse);

        // Act
        AddNoteDto addNote = AddNoteDto.builder().build();
        ResponseEntity<List<NoteDto>> responseEntity = defendantAccountController
            .getNotesForDefendantAccount("1", BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
        verify(noteService, times(1)).searchNotes(any(
            NoteSearchDto.class));

    }

    // TODO - This is Disco+ Code. To Be Removed?
    @Test
    void testNotes_NoContent() {
        // Arrange
        NoteDto mockNote = new NoteDto();

        when(noteService.searchNotes(any(NoteSearchDto.class))).thenReturn(null);

        // Act
        AddNoteDto addNote = AddNoteDto.builder().build();
        ResponseEntity<List<NoteDto>> responseEntity = defendantAccountController
            .getNotesForDefendantAccount("1", BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(null, responseEntity.getBody());
        verify(noteService, times(1)).searchNotes(any(
            NoteSearchDto.class));

    }

    @Test
    void testGetHeaderSummary_Success() {
        // Arrange
        DefendantAccountHeaderSummary mockResponse = new DefendantAccountHeaderSummary();

        when(defendantAccountService.getHeaderSummary(any(), any())).thenReturn(mockResponse);

        // Act
        ResponseEntity<DefendantAccountHeaderSummary> responseEntity = defendantAccountController.getHeaderSummary(
             1L, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
        verify(defendantAccountService, times(1)).getHeaderSummary(any(), any());
    }

}
