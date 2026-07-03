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
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.NoteType;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.service.opal.OpalNotesService;

@ExtendWith(MockitoExtension.class)
class OpalNotesServiceTest {

    @Mock
    private NoteRepository repository;
    @Mock
    private EntityManager em;
    @Mock
    private UserState user;

    @InjectMocks
    private OpalNotesService service;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-05-07T10:15:00Z"), ZoneOffset.UTC);

    // common test data objects (no stubbing here to keep STRICT_STUBS happy)
    private AddNoteRequest request;
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

        // Managed entity returned by em.find(...)
        managedInEm = new DefendantAccountEntity();
        managedInEm.setDefendantAccountId(77L);
        managedInEm.setVersionNumber(2L);
        managedInEm.setBusinessUnit(bu((short) 1));
    }

    @Test
    void addNote_success_savesFields_returnsId_andLocksManagedEntity() {
        when(em.find(DefendantAccountEntity.class, 77L)).thenReturn(managedInEm);
        when(user.getDisplayName()).thenReturn("Normal User");

        // repository.save returns an entity with generated id
        NoteEntity persisted = new NoteEntity();
        persisted.setNoteId(123456789L);
        when(repository.save(any(NoteEntity.class))).thenReturn(persisted);

        // Use a quoted If-Match to exercise the strip-quotes logic
        String returnedId = service.addNote(request, "\"2\"", user, (short) 2);

        assertEquals("123456789", returnedId);

        // Verify saved values
        ArgumentCaptor<NoteEntity> captor = ArgumentCaptor.forClass(NoteEntity.class);
        verify(repository).save(captor.capture());
        NoteEntity toSave = captor.getValue();

        assertEquals("hello world", toSave.getNoteText());
        assertEquals(NoteType.AA, toSave.getNoteType());
        assertEquals("77", toSave.getAssociatedRecordId());
        assertEquals(AssociatedRecordType.DEFENDANT_ACCOUNTS, toSave.getAssociatedRecordType());
        assertEquals("1", toSave.getBusinessUnitUserId()); // short -> "1"
        assertEquals("Normal User", toSave.getPostedByUsername());
        assertNotNull(toSave.getPostedDate(), "postedDate should be set");
        assertEquals(LocalDateTime.of(2026, 5, 7, 10, 15), toSave.getPostedDate());

        // Lock is called on the MANAGED instance
        verify(em).lock(eq(managedInEm), eq(LockModeType.OPTIMISTIC_FORCE_INCREMENT));
        verifyNoMoreInteractions(repository, em);
    }

    @Test
    void addNote_accountNotFound_throws404_andDoesNotSaveOrLock() {
        when(em.find(DefendantAccountEntity.class, 77L)).thenReturn(null);

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> service.addNote(request, "2", user, (short) 2)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertNotNull(ex.getReason());
        assertTrue(ex.getReason().contains("Account 77 not found"));

        verify(repository, never()).save(any());
        verify(em, never()).lock(any(), any());
    }

    @ParameterizedTest
    @MethodSource("invalidAccounts")
    void addNote_accountIdIsNotANumber_throws404_andDoesNotSaveOrLock() {
        Note note = new Note();
        note.setRecordId("NaN");
        note.setRecordType(RecordType.DEFENDANT_ACCOUNTS);
        request = new AddNoteRequest();
        request.setActivityNote(note);

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> service.addNote(request, "2", user, (short) 2)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertNotNull(ex.getReason());
        assertTrue(ex.getReason().contains("Account NaN not found"));

        verify(repository, never()).save(any());
        verify(em, never()).lock(any(), any());
    }

    @Test
    void addNote_versionMismatch_throwsObjectOptimisticLockingFailure_andDoesNotSaveOrLock() {
        // DB version is 2; pass mismatching If-Match "1"
        when(em.find(DefendantAccountEntity.class, 77L)).thenReturn(managedInEm);

        ObjectOptimisticLockingFailureException ex = assertThrows(
            ObjectOptimisticLockingFailureException.class,
            () -> service.addNote(request, "1", user, (short) 2)
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
            () -> service.addNote(request, null, user, (short) 2));

        verify(repository, never()).save(any());
        verify(em, never()).lock(any(), any());
    }

    // ---------- helpers ----------

    private static BusinessUnitEntity bu(short id) {
        BusinessUnitEntity bu = new BusinessUnitEntity();
        bu.setBusinessUnitId(id);
        return bu;
    }

    private static Stream<Arguments> invalidAccounts() {
        return Stream.of(Arguments.of(Note.builder().recordId("NaN").recordType(RecordType.CREDITOR_ACCOUNTS).build()),
            Arguments.of(Note.builder().recordId("77").recordType(RecordType.REPORT_INSTANCES)),
            Arguments.of(Note.builder().recordId("NaN").recordType(RecordType.DEFENDANT_ACCOUNTS))
        );
    }
}
