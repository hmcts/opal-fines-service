package uk.gov.hmcts.opal.service.legacy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
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
import uk.gov.hmcts.opal.dto.legacy.LegacySaveNoteResponseDto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LegacyNoteServiceTest extends LegacyTestsBase {

    @Mock
    private Logger log;

    @InjectMocks
    private LegacyNoteService legacyNoteService;


    @Test
    @SuppressWarnings("unchecked")
    void saveNote_SuccessfulResponse() {
        // Arrange
        mockRestClientPost();

        NoteDto inputNoteDto = NoteDto.builder()
            .noteType("AC")
            .associatedRecordType("defendants_accounts")
            .associatedRecordId("123456")
            .noteText("This is a sample note text.")
            .postedDate(null)
            .postedBy("user123")
            .build();

        final NoteDto expectedNoteDto = inputNoteDto.toBuilder().noteId(12345L).build();

        String jsonBody = """
            {
                "note_id": 12345
            }
            """;

        ResponseEntity<String> successfulResponseEntity = new ResponseEntity<>(jsonBody, HttpStatus.OK);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(LegacySaveNoteRequestDto.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(successfulResponseEntity);

        // Act
        NoteDto resultNoteDto = legacyNoteService.saveNote(inputNoteDto);

        // Assert
        assertEquals(expectedNoteDto, resultNoteDto);
    }

    @Test
    @SuppressWarnings("unchecked")
    void saveNote_FailureBodyResponse() {
        // Arrange
        mockRestClientPost();
        final NoteDto inputNoteDto = new NoteDto();


        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            null, HttpStatus.OK);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(LegacySaveNoteRequestDto.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(unsuccessfulResponseEntity);

        // Act

        LegacyGatewayResponseException lgre = assertThrows(
            LegacyGatewayResponseException.class,
            () -> legacyNoteService.saveNote(inputNoteDto));

        // Assert

        assertNotNull(lgre);
        assertEquals("Received an empty body in the response from the Legacy Gateway.", lgre.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void saveNote_FailureCodeResponse() {
        // Arrange
        mockRestClientPost();
        final NoteDto inputNoteDto = new NoteDto();


        String jsonBody = """
            {
                "note_id": 123456,
            }
            """;

        ResponseEntity<String> unsuccessfulResponseEntity = new ResponseEntity<>(
            jsonBody, HttpStatus.INTERNAL_SERVER_ERROR);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
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
    void saveNote_ValidateRequest() throws IOException, ProcessingException {

        final NoteDto noteDto = NoteDto.builder()
            .associatedRecordId("12345678")
            .associatedRecordType("defendant_accounts")
            .noteText("")
            .postedBy("")
            .build();
        LegacySaveNoteRequestDto requestDto = LegacySaveNoteRequestDto.fromNoteDto(noteDto);

        // Serialize the DTO to JSON using Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModule(new JavaTimeModule());
        String json = objectMapper.writeValueAsString(requestDto);

        String content = Files.readString(
            Paths.get("src/test/resources/schemas/AccountNotes/of_create_note_in.json"),
            StandardCharsets.UTF_8);

        // Parse the JSON schema
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.byDefault();
        JsonSchema schema = schemaFactory.getJsonSchema(JsonLoader.fromString(content));

        // Generate validation report
        ProcessingReport report = schema.validate(JsonLoader.fromString(json));

        // Validate the serialized JSON against the schema
        assertTrue(report.isSuccess());

    }

    @Test
    void saveNote_ValidateResponse() throws IOException, ProcessingException {

        LegacySaveNoteResponseDto responseDto = LegacySaveNoteResponseDto.builder()
            .nodeId(1234567L)
            .build();
        // Serialize the DTO to JSON using Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModule(new JavaTimeModule());
        String json = objectMapper.writeValueAsString(responseDto);

        String content = Files.readString(
            Paths.get("src/test/resources/schemas/AccountNotes/of_create_note_out.json"),
            StandardCharsets.UTF_8);

        // Parse the JSON schema
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.byDefault();
        JsonSchema schema = schemaFactory.getJsonSchema(JsonLoader.fromString(content));

        // Generate validation report
        ProcessingReport report = schema.validate(JsonLoader.fromString(json));

        // Validate the serialized JSON against the schema
        assertTrue(report.isSuccess());

    }

    @Test
    @SuppressWarnings("unchecked")
    void saveNote_ErrorResponse() {
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
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
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
