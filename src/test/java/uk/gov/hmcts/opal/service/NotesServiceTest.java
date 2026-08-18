package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.proxy.NotesProxy;

@ExtendWith(MockitoExtension.class)
class NotesServiceTest {

    private static final String IF_MATCH = "etag-123";
    private static final Short BUSINESS_UNIT_ID = 10;
    private static final Long DEFENDANT_ACCOUNT_ID = 77L;

    @Mock private NotesProxy notesProxy;
    @Mock private UserStateService userStateService;
    @Mock private AccountNoteContextFactory accountNoteContextFactory;
    @Mock private UserState userState;
    @Mock private NoteRepository repository;
    @Mock private EntityManager em;
    @Mock private UserStateV2 user;

    @InjectMocks
    private NotesService notesService;

    private AddNoteRequest request;
    private AccountNoteContext target;

    @BeforeEach
    void setUp() {
        request = addNoteRequest();
        target = new AccountNoteContext(
            DefendantAccountEntity.class,
            DEFENDANT_ACCOUNT_ID,
            BUSINESS_UNIT_ID,
            AssociatedRecordType.DEFENDANT_ACCOUNTS
        );
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
        detachedParam.setVersionNumber(2L); // irrelevant to service; it re-fetches

        // Managed entity returned by em.find(...)
        managedInEm = new DefendantAccountEntity();
        managedInEm.setDefendantAccountId(77L);
        managedInEm.setVersionNumber(2L);
        managedInEm.setBusinessUnit(bu((short) 1));
    }

    @Test
    void addNote_success_savesFields_returnsId_andLocksManagedEntity() {
        when(em.find(DefendantAccountEntity.class, 77L)).thenReturn(managedInEm);
        when(user.getUsername()).thenReturn("Normal User");

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
    void addNote_shouldThrowPermissionNotAllowedException_whenUserLacksPermission() {
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(accountNoteContextFactory.from(request.getActivityNote())).thenReturn(target);
        when(userState.hasBusinessUnitUserWithPermission(
            BUSINESS_UNIT_ID, FinesPermission.ADD_ACCOUNT_ACTIVITY_NOTES)).thenReturn(false);

        assertThrows(
            PermissionNotAllowedException.class,
            () -> notesService.addNote(request, IF_MATCH, BUSINESS_UNIT_ID)
        );
    }

    @Test
    void addNote_shouldDelegateToNotesProxy_whenUserHasPermission() {
        String expectedResponse = "note-id-456";

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(accountNoteContextFactory.from(request.getActivityNote())).thenReturn(target);
        when(userState.hasBusinessUnitUserWithPermission(
            BUSINESS_UNIT_ID, FinesPermission.ADD_ACCOUNT_ACTIVITY_NOTES)).thenReturn(true);
        when(notesProxy.addNote(request, IF_MATCH, userState, target)).thenReturn(expectedResponse);

        String actualResponse = notesService.addNote(request, IF_MATCH, BUSINESS_UNIT_ID);

        assertEquals(expectedResponse, actualResponse);
        verify(notesProxy).addNote(request, IF_MATCH, userState, target);
    }

    private static AddNoteRequest addNoteRequest() {
        Note note = Note.builder()
            .recordType(RecordType.DEFENDANT_ACCOUNTS)
            .recordId(DEFENDANT_ACCOUNT_ID.toString())
            .noteText("test")
            .noteType("AA")
            .build();
        return new AddNoteRequest(note);
    }
}
