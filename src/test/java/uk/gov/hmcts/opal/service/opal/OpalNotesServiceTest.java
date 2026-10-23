package uk.gov.hmcts.opal.service.opal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.opal.common.spring.security.OpalJwtAuthenticationToken;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.common.util.SecurityUtil;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.NoteType;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.generated.model.AddNoteRequestNotes;
import uk.gov.hmcts.opal.generated.model.NoteCommon;
import uk.gov.hmcts.opal.generated.model.NoteCommon.RecordTypeEnum;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.service.AccountNoteContext;
import uk.gov.hmcts.opal.service.AccountNoteContextFactory;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.util.PermissionUtil;

@ExtendWith(MockitoExtension.class)
class OpalNotesServiceTest {

    @Mock private NoteRepository repository;
    @Mock private DefendantAccountRepositoryService defendantAccountRepositoryService;
    @Mock private CreditorAccountRepository creditorAccountRepository;
    @Mock private AccountNoteContextFactory accountNoteContextFactory;
    @Mock private Clock clock;
    @Mock private UserState user;

    @InjectMocks
    private OpalNotesService service;

    @Test
    @DisplayName("addNote saves a note and returns the note id")
    void addNote_shouldSaveNoteAndReturnId() {
        when(clock.instant()).thenReturn(Instant.parse("2026-07-03T10:15:30Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        DefendantAccountEntity managed = new DefendantAccountEntity();
        managed.setVersionNumber(12L);
        BusinessUnitEntity businessUnit = new BusinessUnitEntity();
        businessUnit.setBusinessUnitId((short) 78);
        managed.setBusinessUnit(businessUnit);
        NoteEntity saved = new NoteEntity();
        saved.setNoteId(999L);
        when(defendantAccountRepositoryService.getDefendantAccountByIdForUpdate(77L))
            .thenReturn(managed);
        when(user.getDisplayName()).thenReturn("Test User");
        when(repository.save(any(NoteEntity.class))).thenReturn(saved);
        AddNoteRequestNotes req = buildRequest("77", NoteCommon.RecordTypeEnum.DEFENDANT_ACCOUNTS);

        String result = service.addNote(req, "\"12\"", user, defendantTarget());

        assertThat(result).isEqualTo("999");
        ArgumentCaptor<NoteEntity> captor = ArgumentCaptor.forClass(NoteEntity.class);
        verify(repository).save(captor.capture());
        NoteEntity entity = captor.getValue();
        assertThat(entity.getNoteText()).isEqualTo("test");
        assertThat(entity.getNoteType()).isEqualTo(NoteType.AA);
        assertThat(entity.getAssociatedRecordId()).isEqualTo("77");
        assertThat(entity.getAssociatedRecordType()).isEqualTo(AssociatedRecordType.DEFENDANT_ACCOUNTS);
        assertThat(entity.getBusinessUnitUserId()).isEqualTo("78");
        assertThat(entity.getPostedDate()).isEqualTo(
            LocalDateTime.ofInstant(Instant.parse("2026-07-03T10:15:30Z"), ZoneOffset.UTC));
        assertThat(entity.getPostedByUsername()).isEqualTo("Test User");
        verify(defendantAccountRepositoryService).getDefendantAccountByIdForUpdate(77L);
        verify(defendantAccountRepositoryService).incrementVersionNumber(77L, managed.getVersion());
    }

    @Test
    void addNote_shouldSaveCreditorAccountNote() {
        when(clock.instant()).thenReturn(Instant.parse("2026-07-03T10:15:30Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        CreditorAccountEntity managed = new CreditorAccountEntity();
        managed.setCreditorAccountId(104L);
        managed.setVersionNumber(3L);
        NoteEntity saved = new NoteEntity();
        saved.setNoteId(1001L);
        when(creditorAccountRepository.findByCreditorAccountIdForUpdate(104L)).thenReturn(Optional.of(managed));
        when(user.getDisplayName()).thenReturn("Creditor User");
        when(repository.save(any(NoteEntity.class))).thenReturn(saved);
        AddNoteRequestNotes req = buildRequest("104", NoteCommon.RecordTypeEnum.CREDITOR_ACCOUNTS);

        String result = service.addNote(req, "\"3\"", user, creditorTarget());

        assertThat(result).isEqualTo("1001");
        ArgumentCaptor<NoteEntity> captor = ArgumentCaptor.forClass(NoteEntity.class);
        verify(repository).save(captor.capture());
        NoteEntity entity = captor.getValue();
        assertThat(entity.getAssociatedRecordId()).isEqualTo("104");
        assertThat(entity.getAssociatedRecordType()).isEqualTo(AssociatedRecordType.CREDITOR_ACCOUNTS);
        assertThat(entity.getBusinessUnitUserId()).isEqualTo("78");
        assertThat(entity.getPostedByUsername()).isEqualTo("Creditor User");
        verify(creditorAccountRepository).findByCreditorAccountIdForUpdate(104L);
    }

    @Test
    void addNote_shouldResolveAccountContextForOpalRoute() {
        when(clock.instant()).thenReturn(Instant.parse("2026-07-03T10:15:30Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        DefendantAccountEntity managed = new DefendantAccountEntity();
        managed.setVersionNumber(12L);
        BusinessUnitEntity businessUnit = new BusinessUnitEntity();
        businessUnit.setBusinessUnitId((short) 78);
        managed.setBusinessUnit(businessUnit);
        NoteEntity saved = new NoteEntity();
        saved.setNoteId(999L);
        AddNoteRequestNotes req = buildRequest("77", RecordTypeEnum.DEFENDANT_ACCOUNTS);
        AccountNoteContext target = defendantTarget();
        when(accountNoteContextFactory.from(req.getActivityNote())).thenReturn(target);
        when(defendantAccountRepositoryService.getDefendantAccountByIdForUpdate(77L))
            .thenReturn(managed);
        when(user.getDisplayName()).thenReturn("Test User");
        when(repository.save(any(NoteEntity.class))).thenReturn(saved);

        String result = service.addNote(req, "\"12\"", user, (short) 78);

        assertThat(result).isEqualTo("999");
        verify(accountNoteContextFactory).from(req.getActivityNote());
        verify(defendantAccountRepositoryService).getDefendantAccountByIdForUpdate(77L);
    }

    @Nested
    @DisplayName("Add a note using record details and the authenticated user")
    class AddNoteWithRecordDetails {

        private static final Short BUSINESS_UNIT_ID = 78;
        private static final String NOTE_TEXT = "Alternate payment reference added | REFERENCE-1";
        private static final String BUSINESS_UNIT_USER_ID = "USER-78";
        private static final String USERNAME = "notes-user@example.com";

        @Mock
        private OpalJwtAuthenticationToken token;

        @Mock
        private UserStateV2 userState;

        private MockedStatic<SecurityUtil> securityUtil;
        private MockedStatic<PermissionUtil> permissionUtil;

        @BeforeEach
        void setUpAuthenticatedUser() {
            when(token.getUserState()).thenReturn(userState);
            securityUtil = mockStatic(SecurityUtil.class);
            permissionUtil = mockStatic(PermissionUtil.class);
            securityUtil.when(SecurityUtil::getOpalJwtAuthenticationTokenForCurrentUser).thenReturn(token);
            permissionUtil.when(() -> PermissionUtil.getRequiredBusinessUnitUser(userState, BUSINESS_UNIT_ID))
                .thenReturn(BusinessUnitUser.builder().businessUnitUserId(BUSINESS_UNIT_USER_ID).build());
        }

        @AfterEach
        void closeStaticMocks() {
            permissionUtil.close();
            securityUtil.close();
        }

        @Test
        @DisplayName("Saves a defendant account note with user details and timestamps before incrementing its version")
        void savesDefendantAccountNoteBeforeIncrementingSuppliedAccountVersion() {
            stubPostingDetails();
            BigInteger version = BigInteger.valueOf(12L);

            service.addNote(NoteType.AA, AssociatedRecordType.DEFENDANT_ACCOUNTS,
                77L, version, NOTE_TEXT, BUSINESS_UNIT_ID);

            NoteEntity note = assertSavedNote(AssociatedRecordType.DEFENDANT_ACCOUNTS, "77");
            InOrder order = inOrder(repository, defendantAccountRepositoryService);
            order.verify(repository).save(note);
            order.verify(defendantAccountRepositoryService).incrementVersionNumber(77L, version);
            verifyNoInteractions(creditorAccountRepository, accountNoteContextFactory);
        }

        @Test
        @DisplayName("Saves a creditor account note without incrementing a defendant account version")
        void savesCreditorAccountNoteWithoutIncrementingDefendantAccountVersion() {
            stubPostingDetails();

            service.addNote(NoteType.AA, AssociatedRecordType.CREDITOR_ACCOUNTS,
                104L, BigInteger.valueOf(3L), NOTE_TEXT, BUSINESS_UNIT_ID);

            assertSavedNote(AssociatedRecordType.CREDITOR_ACCOUNTS, "104");
            verifyNoInteractions(defendantAccountRepositoryService, creditorAccountRepository,
                accountNoteContextFactory);
        }

        @Test
        @DisplayName("Does not save a note or increment the account version when the business unit user is unavailable")
        void throwsAccessDeniedExceptionWithoutSavingOrIncrementingVersionWhenBusinessUnitUserIsUnavailable() {
            AccessDeniedException expected = new AccessDeniedException("User is not assigned to this business unit");
            permissionUtil.when(() -> PermissionUtil.getRequiredBusinessUnitUser(userState, BUSINESS_UNIT_ID))
                .thenThrow(expected);

            AccessDeniedException actual = assertThrows(AccessDeniedException.class,
                () -> service.addNote(NoteType.AA, AssociatedRecordType.DEFENDANT_ACCOUNTS,
                    77L, BigInteger.valueOf(12L), NOTE_TEXT, BUSINESS_UNIT_ID));

            assertSame(expected, actual);
            verifyNoInteractions(repository, defendantAccountRepositoryService);
        }

        @Test
        @DisplayName("Does not increment the account version when saving the note fails")
        void propagatesSaveFailureWithoutIncrementingAccountVersion() {
            stubPostingDetails();
            DataIntegrityViolationException expected = new DataIntegrityViolationException("Note could not be saved");
            when(repository.save(any(NoteEntity.class))).thenThrow(expected);

            DataIntegrityViolationException actual = assertThrows(DataIntegrityViolationException.class,
                () -> service.addNote(NoteType.AA, AssociatedRecordType.DEFENDANT_ACCOUNTS,
                    77L, BigInteger.valueOf(12L), NOTE_TEXT, BUSINESS_UNIT_ID));

            assertSame(expected, actual);
            verify(repository).save(any(NoteEntity.class));
            verifyNoInteractions(defendantAccountRepositoryService);
        }

        private void stubPostingDetails() {
            when(clock.instant()).thenReturn(Instant.parse("2026-10-22T10:15:30Z"));
            when(clock.getZone()).thenReturn(ZoneOffset.UTC);
            when(token.getUsername()).thenReturn(USERNAME);
        }

        private NoteEntity assertSavedNote(AssociatedRecordType recordType, String recordId) {
            ArgumentCaptor<NoteEntity> captor = ArgumentCaptor.forClass(NoteEntity.class);
            verify(repository).save(captor.capture());
            NoteEntity note = captor.getValue();
            assertThat(note.getNoteText()).isEqualTo(NOTE_TEXT);
            assertThat(note.getNoteType()).isEqualTo(NoteType.AA);
            assertThat(note.getAssociatedRecordId()).isEqualTo(recordId);
            assertThat(note.getAssociatedRecordType()).isEqualTo(recordType);
            assertThat(note.getPostedDate()).isEqualTo(LocalDateTime.of(2026, 10, 22, 10, 15, 30));
            assertThat(note.getPostedByUsername()).isEqualTo(USERNAME);
            assertThat(note.getBusinessUnitUserId()).isEqualTo(BUSINESS_UNIT_USER_ID);
            securityUtil.verify(SecurityUtil::getOpalJwtAuthenticationTokenForCurrentUser);
            permissionUtil.verify(() -> PermissionUtil.getRequiredBusinessUnitUser(userState, BUSINESS_UNIT_ID));
            return note;
        }
    }

    private static AddNoteRequestNotes buildRequest(String recordId, NoteCommon.RecordTypeEnum recordType) {
        NoteCommon note = new NoteCommon();
        note.setRecordId(recordId);
        note.setRecordType(recordType);
        note.setNoteText("test");
        note.setNoteType(NoteCommon.NoteTypeEnum.AA);
        return AddNoteRequestNotes.builder().activityNote(note).build();
    }

    private static AccountNoteContext defendantTarget() {
        return new AccountNoteContext(
            DefendantAccountEntity.class,
            77L,
            (short) 78,
            AssociatedRecordType.DEFENDANT_ACCOUNTS
        );
    }

    private static AccountNoteContext creditorTarget() {
        return new AccountNoteContext(
            CreditorAccountEntity.class,
            104L,
            (short) 78,
            AssociatedRecordType.CREDITOR_ACCOUNTS
        );
    }
}
