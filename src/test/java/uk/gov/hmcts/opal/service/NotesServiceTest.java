package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.service.opal.OpalNotesService;

@ExtendWith(MockitoExtension.class)
public class NotesServiceTest {


    @Mock
    private DefendantAccountRepository defendantAccountRepository;
    @Mock
    private NoteRepository repository;
    @Mock
    private EntityManager em;

    @InjectMocks
    private OpalNotesService service; // the class that implements addNote(...)

    private DefendantAccountEntity account;
    private AddNoteRequest addNoteRequest;

    @BeforeEach
    void setUp() {
        // Arrange a request with a Note payload
        Note requestNote = new Note();
        requestNote.setRecordId("42");
        requestNote.setRecordType(RecordType.DEFENDANT_ACCOUNTS);
        requestNote.setNoteText("hello");
        requestNote.setNoteType("AA");

        addNoteRequest = new AddNoteRequest();
        addNoteRequest.setActivityNote(requestNote);

        // Account found in DB with version 5
        account = new DefendantAccountEntity();
        account.setDefendantAccountId(42L);
        account.setVersion(5L);
        account.setBusinessUnit(BusinessUnitEntity.builder().businessUnitId((short) 1).build());
    }

    @Test
    void addNote_success_savesFields_returnsId_andLocksAccount() {
        // Given
        when(defendantAccountRepository.findById(42L)).thenReturn(Optional.of(account));

        // repository.save returns an entity with a generated id
        NoteEntity persisted = new NoteEntity();
        persisted.setNoteId(60000000000000L);
        when(repository.save(any(NoteEntity.class))).thenReturn(persisted);

        // When
        String id = service.addNote(addNoteRequest, /*If-Match version*/ 5L, "USER1");

        // Then: return value
        assertEquals("60000000000000", id);

        // Then: saved fields are correct
        ArgumentCaptor<NoteEntity> noteCaptor = ArgumentCaptor.forClass(NoteEntity.class);
        verify(repository).save(noteCaptor.capture());
        NoteEntity toSave = noteCaptor.getValue();
        assertEquals("hello", toSave.getNoteText());
        assertEquals("AA", toSave.getNoteType());
        assertEquals("42", toSave.getAssociatedRecordId());
        assertEquals(RecordType.DEFENDANT_ACCOUNTS.toString(), toSave.getAssociatedRecordType());

        // Then: account was locked with OPTIMISTIC_FORCE_INCREMENT
        verify(em).lock(eq(account), eq(LockModeType.OPTIMISTIC_FORCE_INCREMENT));

        // And: no extra interactions
        verifyNoMoreInteractions(repository, defendantAccountRepository, em);
    }

    @Test
    void addNote_accountNotFound_throws404() {
        // Given
        when(defendantAccountRepository.findById(42L)).thenReturn(Optional.empty());

        // When
        ResponseStatusException ex =
            assertThrows(ResponseStatusException.class,
                         () -> service.addNote(addNoteRequest, 5L, "USER1"));

        // Then
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertNotNull(ex.getReason());
        assertTrue(ex.getReason().contains("Account 42 not found"));
        verify(repository, never()).save(any());
        verify(em, never()).lock(any(), any());
    }

    @Test
    void addNote_versionMismatch_throws412() {
        // Given
        when(defendantAccountRepository.findById(42L)).thenReturn(Optional.of(account));
        // account.version = 5L from setUp(), but we pass 6L
        // When
        ResponseStatusException ex =
            assertThrows(ResponseStatusException.class,
                         () -> service.addNote(addNoteRequest, 6L, "USER1"));

        // Then
        assertEquals(HttpStatus.PRECONDITION_FAILED, ex.getStatusCode());
        assertNotNull(ex.getReason());
        assertTrue(ex.getReason().contains("Expected 5 but got 6"));
        verify(repository, never()).save(any());
        verify(em, never()).lock(any(), any());
    }
}
