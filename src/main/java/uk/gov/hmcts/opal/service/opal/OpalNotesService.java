package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.dto.RecordType.DEFENDANT_ACCOUNTS;
import static uk.gov.hmcts.opal.util.VersionUtils.verifyIfMatch;

import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.NoteType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.service.iface.NotesServiceInterface;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;

@Service
@Slf4j(topic = "opal.OpalNotesService")
@RequiredArgsConstructor
public class OpalNotesService implements NotesServiceInterface {

    private final NoteRepository repository;
    private final DefendantAccountRepositoryService defendantAccountRepositoryService;
    private final Clock clock;

    @Override
    @Transactional
    public String addNote(AddNoteRequest req, String ifMatch, UserState user, Short businessUnitId) {
        log.info(":OpalAddNote");

        String accountId = req.getActivityNote().getRecordId();
        validateAccountId(req, accountId);
        //Use getDefendantAccountByIdForUpdate() as this ensures the account version is increased with
        // OPTIMISTIC_FORCE_INCREMENT locking
        DefendantAccountEntity managed =
            defendantAccountRepositoryService.getDefendantAccountByIdForUpdate(Long.parseLong(accountId));

        verifyIfMatch(managed, ifMatch, managed.getVersion(), "addNote");

        Note requestNote = req.getActivityNote();
        NoteEntity note = new NoteEntity();
        note.setNoteText(requestNote.getNoteText());
        note.setNoteType(NoteType.valueOf(requestNote.getNoteType()));
        note.setAssociatedRecordId(requestNote.getRecordId());
        note.setAssociatedRecordType(AssociatedRecordType.DEFENDANT_ACCOUNTS);
        note.setBusinessUnitUserId(managed.getBusinessUnit().getBusinessUnitId().toString());
        note.setPostedDate(LocalDateTime.now(clock));
        note.setPostedByUsername(user.getDisplayName());
        NoteEntity entity = repository.save(note);
        return entity.getNoteId().toString();
    }

    private static void validateAccountId(AddNoteRequest req, String accountId) {
        if (!Objects.equals(req.getActivityNote().getRecordType(), DEFENDANT_ACCOUNTS)) {
            throw new IllegalArgumentException(
                "recordType must be '%s'".formatted(DEFENDANT_ACCOUNTS)
            );
        }

        if (!NumberUtils.isCreatable(accountId)) {
            throw new IllegalArgumentException(
                "recordId must be a numeric value"
            );
        }
    }

}
