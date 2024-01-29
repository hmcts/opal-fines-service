package uk.gov.hmcts.opal.service;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.dto.legacy.LegacySaveNoteRequestDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LegacyProxyNoteServiceTest extends LegacyTestsBase {

    @Mock
    private Logger log;

    @InjectMocks
    private LegacyNoteService legacyNoteService;


    @Test
    @SuppressWarnings("unchecked")
    void saveNote_SuccessfulResponse() throws Exception {
        // Arrange
        mockRestClientPost();

        NoteDto inputNoteDto = NoteDto.builder()
            .noteId(1L)
            .noteType("AC")
            .associatedRecordType("defendants_accounts")
            .associatedRecordId("123456")
            .noteText("This is a sample note text.")
            .postedDate(null)
            .postedBy("user123")
            .build();

        NoteDto expectedNoteDto = inputNoteDto.toBuilder().noteId(123456L).build();

        String jsonBody = """
            {
                "note_id": 123456
            }
            """;

        ResponseEntity<String> successfulResponseEntity = new ResponseEntity<>(jsonBody, HttpStatus.OK);
        when(requestBodySpec.body(any(LegacySaveNoteRequestDto.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(successfulResponseEntity);
        // Act
        NoteDto resultNoteDto = legacyNoteService.saveNote(inputNoteDto);

        // Assert
        assertEquals(expectedNoteDto, resultNoteDto);
    }

    @Test
    @SuppressWarnings("unchecked")
    void saveNote_FailureBodyResponse() throws Exception {
        // Arrange
        mockRestClientPost();
        final NoteDto inputNoteDto = new NoteDto();


        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            null, HttpStatus.OK);
        when(requestBodySpec.body(any(LegacySaveNoteRequestDto.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(unsuccessfulResponseEntity);
        // Act
        LegacyGatewayResponseException lgre = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyNoteService.saveNote(inputNoteDto));

        // Assert

        assertNotNull(lgre);
        assertEquals("Received an empty body in the response from the Legacy Gateway.",
                     lgre.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void saveNote_FailureCodeResponse() throws Exception {
        // Arrange
        mockRestClientPost();
        final NoteDto inputNoteDto = new NoteDto();


        String jsonBody = """
            {
            "noteType": "AC",
            "associatedRecordType": "defendants_accounts",
            "associatedRecordId": "123456",
            "noteText": "This is a sample note text.",
            "postedBy": "user123"
            }
            """;

        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            jsonBody, HttpStatus.INTERNAL_SERVER_ERROR);
        when(requestBodySpec.body(any(LegacySaveNoteRequestDto.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(unsuccessfulResponseEntity);
        // Act
        LegacyGatewayResponseException lgre = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyNoteService.saveNote(inputNoteDto));

        // Assert

        assertNotNull(lgre);
        assertEquals("Received a non-2xx response from the Legacy Gateway: 500 INTERNAL_SERVER_ERROR",
                     lgre.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void saveNote_ErrorResponse() throws Exception {
        // Arrange
        mockRestClientPost();
        final NoteDto inputNoteDto = new NoteDto();


        String jsonBody = """
            {
            "noteTypeFOOBAR": "AC",
            "associatedRecordType": "defendants_accounts",
            "associatedRecordId": "123456",
            "noteText": "This is a sample note text.",
            "postedBy": "user123"
            }
            """;


        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            jsonBody, HttpStatus.OK);
        when(requestBodySpec.body(any(LegacySaveNoteRequestDto.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(unsuccessfulResponseEntity);
        // Act
        LegacyGatewayResponseException lgre = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyNoteService.saveNote(inputNoteDto));

        // Assert

        assertNotNull(lgre);
        Throwable cause = lgre.getCause();
        assertNotNull(cause);
        assertEquals(UnrecognizedPropertyException.class, cause.getClass());
    }

}
