package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.dto.RecordType.DEFENDANT_ACCOUNTS;
import static uk.gov.hmcts.opal.util.VersionUtils.verifyIfMatch;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.NoteType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.service.iface.NotesServiceInterface;

@Service
@Slf4j(topic = "opal.OpalNotesService")
@RequiredArgsConstructor
public class OpalNotesService implements NotesServiceInterface {

    private final NoteRepository repository;
    private final EntityManager em;
    private final Clock clock;

    @Override
    @Transactional
    public String addNote(AddNoteRequest req, String ifMatch, UserState user, Short businessUnitId) {
        // TODO - waiting for PO-1564 to call DefendantAccountService to get the account

        log.info(":OpalAddNote");

        String accountId = req.getActivityNote().getRecordId();
        if (!Objects.equals(req.getActivityNote().getRecordType(), DEFENDANT_ACCOUNTS) || !NumberUtils.isCreatable(
            accountId)) {
            throw accountNotFound(accountId);
        }
        DefendantAccountEntity managed = em.find(DefendantAccountEntity.class, Long.valueOf(accountId));
        if (managed == null) {
            throw accountNotFound(accountId);
        }
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

        // IMPORTANT: lock the MANAGED instance, not the detached parameter
        em.lock(managed, LockModeType.OPTIMISTIC_FORCE_INCREMENT);

        return entity.getNoteId().toString();
    }

    private ResponseStatusException accountNotFound(String accountId) {
        return new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Account %s not found".formatted(accountId)
        );
    }

}
