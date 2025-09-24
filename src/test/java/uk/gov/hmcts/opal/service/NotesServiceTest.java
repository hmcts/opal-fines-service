package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.service.opal.OpalNotesService;
@ExtendWith(MockitoExtension.class)
class NotesServiceTest {

    @Mock private NoteRepository repository;
    @Mock private EntityManager em;
    @Mock private UserState user;

    @InjectMocks private OpalNotesService service;

    private DefendantAccountEntity account;
    private AddNoteRequest addNoteRequest;

    @BeforeEach
    void setUp() {
        // Build request
        Note requestNote = new Note();
        requestNote.setRecordId("42");
        requestNote.setRecordType(RecordType.DEFENDANT_ACCOUNTS);
        requestNote.setNoteText("hello");
        requestNote.setNoteType("AA");

        addNoteRequest = new AddNoteRequest();
        addNoteRequest.setActivityNote(requestNote);

        // Build account (passed directly to service)
        account = new DefendantAccountEntity();
        account.setDefendantAccountId(42L);
        account.setVersion(5L);
        account.setBusinessUnit(BusinessUnitEntity.builder().businessUnitId((short) 1).build());
    }

    @Test
    void addNote_success_savesFields_returnsId_andLocksAccount() {
        // Stubs used by this test only
        when(user.getUserName()).thenReturn("USER1");

        NoteEntity persisted = new NoteEntity();
        persisted.setNoteId(60000000000000L);
        when(repository.save(any(NoteEntity.class))).thenReturn(persisted);

        String id = service.addNote(addNoteRequest, 5L, user, account);

        assertEquals("60000000000000", id);

        var captor = ArgumentCaptor.forClass(NoteEntity.class);
        verify(repository).save(captor.capture());
        NoteEntity toSave = captor.getValue();
        assertEquals("hello", toSave.getNoteText());
        assertEquals("AA", toSave.getNoteType());
        assertEquals("42", toSave.getAssociatedRecordId());
        assertEquals(RecordType.DEFENDANT_ACCOUNTS.toString(), toSave.getAssociatedRecordType());
        assertEquals("1", toSave.getBusinessUnitUserId());

        verify(em).lock(eq(account), eq(LockModeType.OPTIMISTIC_FORCE_INCREMENT));
        verifyNoMoreInteractions(repository, em);
    }

    @Test
    void addNote_versionMismatch_throws412_andDoesNotSaveOrLock() {
        ResponseStatusException ex =
            assertThrows(ResponseStatusException.class,
                         () -> service.addNote(addNoteRequest, 6L, user, account));

        assertEquals(HttpStatus.PRECONDITION_FAILED, ex.getStatusCode());
        Assertions.assertNotNull(ex.getReason());
        assertTrue(ex.getReason().contains("Expected 5 but got 6"));

        verifyNoInteractions(repository, em);  // nothing saved/locked
    }
}
