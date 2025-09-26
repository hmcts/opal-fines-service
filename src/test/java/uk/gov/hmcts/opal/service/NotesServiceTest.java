package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.service.opal.OpalNotesService;

@ExtendWith(MockitoExtension.class)
class NotesServiceTest {

    @Mock private NoteRepository repository;
    @Mock private EntityManager em;
    @Mock private UserState user;

    @InjectMocks private OpalNotesService service;

    // common test data objects (no stubbing here to keep STRICT_STUBS happy)
    private AddNoteRequest request;
    private DefendantAccountEntity detachedParam;
    private DefendantAccountEntity managedInEm;

    @BeforeEach
    void setUp() {
        // Build request payload
        Note n = new Note();
        n.setRecordId("77");
        n.setRecordType(RecordType.DEFENDANT_ACCOUNTS);
        n.setNoteText("hello world");
        n.setNoteType("AA");

        request = new AddNoteRequest();
        request.setActivityNote(n);

        // Detached param passed by caller
        detachedParam = new DefendantAccountEntity();
        detachedParam.setDefendantAccountId(77L);
        detachedParam.setVersion(2L); // irrelevant to service; it re-fetches

        // Managed entity returned by em.find(...)
        managedInEm = new DefendantAccountEntity();
        managedInEm.setDefendantAccountId(77L);
        managedInEm.setVersion(2L);
        managedInEm.setBusinessUnit(bu((short) 1));
    }

    @Test
    void addNote_success_savesFields_returnsId_andLocksManagedEntity() {
        when(em.find(DefendantAccountEntity.class, 77L)).thenReturn(managedInEm);
        when(user.getUserName()).thenReturn("USER1");

        // repository.save returns an entity with generated id
        NoteEntity persisted = new NoteEntity();
        persisted.setNoteId(123456789L);
        when(repository.save(any(NoteEntity.class))).thenReturn(persisted);

        // Use a quoted If-Match to exercise the strip-quotes logic
        String returnedId = service.addNote(request, "\"2\"", user, detachedParam);

        assertEquals("123456789", returnedId);

        // Verify saved values
        ArgumentCaptor<NoteEntity> captor = ArgumentCaptor.forClass(NoteEntity.class);
        verify(repository).save(captor.capture());
        NoteEntity toSave = captor.getValue();

        assertEquals("hello world", toSave.getNoteText());
        assertEquals("AA", toSave.getNoteType());
        assertEquals("77", toSave.getAssociatedRecordId());
        assertEquals(RecordType.DEFENDANT_ACCOUNTS.toString(), toSave.getAssociatedRecordType());
        assertEquals("1", toSave.getBusinessUnitUserId()); // short -> "1"
        assertEquals("USER1", toSave.getPostedByUsername());
        assertNotNull(toSave.getPostedDate(), "postedDate should be set");
        assertFalse(toSave.getPostedDate().isAfter(LocalDateTime.now()));

        // Lock is called on the MANAGED instance
        verify(em).lock(eq(managedInEm), eq(LockModeType.OPTIMISTIC_FORCE_INCREMENT));
        verifyNoMoreInteractions(repository, em);
    }

    @Test
    void addNote_accountNotFound_throws404_andDoesNotSaveOrLock() {
        when(em.find(DefendantAccountEntity.class, 77L)).thenReturn(null);

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> service.addNote(request, "2", user, detachedParam)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertNotNull(ex.getReason());
        assertTrue(ex.getReason().contains("Account 77 not found"));

        verify(repository, never()).save(any());
        verify(em, never()).lock(any(), any());
    }

    @Test
    void addNote_versionMismatch_throwsObjectOptimisticLockingFailure_andDoesNotSaveOrLock() {
        // DB version is 2; pass mismatching If-Match "1"
        when(em.find(DefendantAccountEntity.class, 77L)).thenReturn(managedInEm);

        ObjectOptimisticLockingFailureException ex = assertThrows(
            ObjectOptimisticLockingFailureException.class,
            () -> service.addNote(request, "1", user, detachedParam)
        );

        assertTrue(ex.getMessage() == null || ex.getMessage().contains("Version")
                       || ex.getMessage().contains("match"), "Expected version mismatch message");

        verify(repository, never()).save(any());
        verify(em, never()).lock(any(), any());
    }

    @Test
    void addNote_ifMatchNull_throwsResourceConflict_andDoesNotSaveOrLock() {
        when(em.find(DefendantAccountEntity.class, 77L)).thenReturn(managedInEm);

        assertThrows(ResourceConflictException.class,
                     () -> service.addNote(request, null, user, detachedParam));

        verify(repository, never()).save(any());
        verify(em, never()).lock(any(), any());
    }

    // ---------- helpers ----------

    private static BusinessUnitEntity bu(short id) {
        BusinessUnitEntity bu = new BusinessUnitEntity();
        bu.setBusinessUnitId(id);
        return bu;
    }
}
