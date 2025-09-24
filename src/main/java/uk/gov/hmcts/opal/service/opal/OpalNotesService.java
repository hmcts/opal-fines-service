package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.service.iface.NotesServiceInterface;

@Service
@Slf4j(topic = "opal.OpalNotesService")
@RequiredArgsConstructor
public class OpalNotesService implements NotesServiceInterface {

    private final NoteRepository repository;
    private final EntityManager em;

    @Override
    @Transactional
    public String addNote(AddNoteRequest req, Long version, UserState user, DefendantAccountEntity account) {
        // Reattach / ensure managed
        Long accountId = account.getDefendantAccountId(); // use your actual ID getter
        DefendantAccountEntity managed = em.find(DefendantAccountEntity.class, accountId);

        if (managed == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Account %s not found".formatted(accountId)
            );
        }

        // Use the MANAGED entity for all reads
        if (!managed.getVersion().equals(version)) {
            throw new ResponseStatusException(
                HttpStatus.PRECONDITION_FAILED,
                "Version mismatch. Expected "
                    + managed.getVersion() + " but got " + version
            );
        }

        Note requestNote = req.getActivityNote();

        NoteEntity note = new NoteEntity();
        note.setNoteText(requestNote.getNoteText());
        note.setNoteType(requestNote.getNoteType());
        note.setAssociatedRecordId(requestNote.getRecordId());
        note.setAssociatedRecordType(requestNote.getRecordType().toString());
        note.setBusinessUnitUserId(managed.getBusinessUnit().getBusinessUnitId().toString());
        note.setPostedDate(LocalDateTime.now());
        note.setPostedByUsername(user.getUserName());

        NoteEntity entity = repository.save(note);

        // IMPORTANT: lock the MANAGED instance, not the detached parameter
        em.lock(managed, LockModeType.OPTIMISTIC_FORCE_INCREMENT);

        return entity.getNoteId().toString();
    }

}
