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

    private AddNoteRequest request;
    private DefendantAccountEntity detachedParam;
    private DefendantAccountEntity managedInEm;

    @BeforeEach
    void setUp() {
        // Build request
        Note n = new Note();
        n.setRecordId("77");
        n.setRecordType(RecordType.DEFENDANT_ACCOUNTS);
        n.setNoteText("hello world");
        n.setNoteType("AA");

        request = new AddNoteRequest();
        request.setActivityNote(n);

        // Detached param
        detachedParam = new DefendantAccountEntity();
        detachedParam.setDefendantAccountId(77L);
        detachedParam.setVersion(1L);
        detachedParam.setBusinessUnit(bu());

        // Managed entity returned by em.find(...)
        managedInEm = new DefendantAccountEntity();
        managedInEm.setDefendantAccountId(77L);
        managedInEm.setVersion(1L);
        managedInEm.setBusinessUnit(bu());
    }

    @Test
    void addNote_success_savesFields_returnsId_andLocksManagedEntity() {
        when(em.find(DefendantAccountEntity.class, 77L)).thenReturn(managedInEm);
        when(user.getUserName()).thenReturn("USER1");                  // <-- stub ONLY here
        NoteEntity persisted = new NoteEntity();
        persisted.setNoteId(123456789L);
        when(repository.save(any(NoteEntity.class))).thenReturn(persisted); // <-- stub ONLY here

        String id = service.addNote(request, 1L, user, detachedParam);
        assertEquals("123456789", id);

        var captor = ArgumentCaptor.forClass(NoteEntity.class);
        verify(repository).save(captor.capture());
        NoteEntity toSave = captor.getValue();
        assertEquals("hello world", toSave.getNoteText());
        assertEquals("AA", toSave.getNoteType());
        assertEquals("77", toSave.getAssociatedRecordId());
        assertEquals(RecordType.DEFENDANT_ACCOUNTS.toString(), toSave.getAssociatedRecordType());
        assertEquals("1", toSave.getBusinessUnitUserId());
        assertEquals("USER1", toSave.getPostedByUsername());
        assertNotNull(toSave.getPostedDate());

        verify(em).lock(eq(managedInEm), eq(LockModeType.OPTIMISTIC_FORCE_INCREMENT));
        verifyNoMoreInteractions(repository, em);
    }

    @Test
    void addNote_accountNotFound_throws404_andDoesNotSaveOrLock() {
        when(em.find(DefendantAccountEntity.class, 77L)).thenReturn(null);

        ResponseStatusException ex =
            assertThrows(ResponseStatusException.class,
                         () -> service.addNote(request, 1L, user, detachedParam));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(repository, never()).save(any());
        verify(em, never()).lock(any(), any());
    }

    @Test
    void addNote_versionMismatch_throws412_andDoesNotSaveOrLock() {
        managedInEm.setVersion(2L);
        when(em.find(DefendantAccountEntity.class, 77L)).thenReturn(managedInEm);

        ResponseStatusException ex =
            assertThrows(ResponseStatusException.class,
                         () -> service.addNote(request, 1L, user, detachedParam));

        assertEquals(HttpStatus.PRECONDITION_FAILED, ex.getStatusCode());
        assertNotNull(ex.getReason());
        assertTrue(ex.getReason().contains("Expected 2 but got 1"));
        verify(repository, never()).save(any());
        verify(em, never()).lock(any(), any());
    }

    private static BusinessUnitEntity bu() {
        BusinessUnitEntity bu = new BusinessUnitEntity();
        bu.setBusinessUnitId((short) 1);
        return bu;
    }
}
