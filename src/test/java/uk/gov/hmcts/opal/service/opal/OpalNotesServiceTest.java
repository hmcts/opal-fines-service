package uk.gov.hmcts.opal.service.opal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.NoteType;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.generated.model.ActivityNoteNotes;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.service.AccountNoteContext;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.generated.model.AddNoteRequestNotes;
import uk.gov.hmcts.opal.generated.model.NoteNotes;

@ExtendWith(MockitoExtension.class)
class OpalNotesServiceTest {

    @Mock private NoteRepository repository;
    @Mock private DefendantAccountRepositoryService defendantAccountRepositoryService;
    @Mock private CreditorAccountRepository creditorAccountRepository;
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
        AddNoteRequestNotes req = buildRequest("77", ActivityNoteNotes.RecordTypeEnum.DEFENDANT_ACCOUNTS);

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
        AddNoteRequestNotes req = buildRequest("104", ActivityNoteNotes.RecordTypeEnum.CREDITOR_ACCOUNTS);

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

    private static AddNoteRequestNotes buildRequest(String recordId, ActivityNoteNotes.RecordTypeEnum recordType) {
        ActivityNoteNotes note = new ActivityNoteNotes();
        note.setRecordId(recordId);
        note.setRecordType(recordType);
        note.setNoteText("test");
        note.setNoteType(ActivityNoteNotes.NoteTypeEnum.AA);
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
