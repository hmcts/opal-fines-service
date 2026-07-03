package uk.gov.hmcts.opal.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.NoteType;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.service.opal.OpalNotesService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;

@ExtendWith(MockitoExtension.class)
class OpalNotesServiceTest {

    @Mock
    private NoteRepository repository;

    @Mock
    private DefendantAccountRepositoryService defendantAccountRepositoryService;

    @Mock
    private Clock clock;

    @Mock
    private UserState user;

    @InjectMocks
    private OpalNotesService service;

    @Test
    @DisplayName("addNote saves a note and returns the note id")
    void addNote_shouldSaveNoteAndReturnId() {
        // Arrange
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
        AddNoteRequest req = buildRequest("77", RecordType.DEFENDANT_ACCOUNTS);

        // Act
        String result = service.addNote(req, "\"12\"", user, (short) 78);

        // Assert
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
    void addNote_shouldThrowWhenRecordTypeIsInvalid() {
        AddNoteRequest req = buildRequest("77", RecordType.CREDITOR_ACCOUNTS);

        assertThatThrownBy(() -> service.addNote(req, "\"12\"", user, (short) 78))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("recordType must be 'defendant_accounts'");
    }

    @Test
    void addNote_shouldThrowWhenRecordIdIsNotNumeric() {
        AddNoteRequest req = buildRequest("abc", RecordType.DEFENDANT_ACCOUNTS);

        assertThatThrownBy(() -> service.addNote(req, "\"12\"", user, (short) 78))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("recordId must be a numeric value");
    }

    private static AddNoteRequest buildRequest(String recordId, RecordType recordType) {
        Note note = new Note();
        note.setRecordId(recordId);
        note.setRecordType(recordType);
        note.setNoteText("test");
        note.setNoteType("AA");
        AddNoteRequest req = new AddNoteRequest();
        req.setActivityNote(note);
        return req;
    }
}
