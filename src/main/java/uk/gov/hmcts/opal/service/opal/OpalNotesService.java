package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.service.iface.NotesServiceInterface;

@Service
@Slf4j(topic = "opal.OpalNotesService")
@RequiredArgsConstructor
public class OpalNotesService implements NotesServiceInterface {

    private final NoteRepository repository;
    private final DefendantAccountRepository defendantAccountRepository;
    private final EntityManager em;

    @Override
    @Transactional
    public String addNote(AddNoteRequest req, Long version) {
        Note requestNote = req.getActivityNote();

        DefendantAccountEntity account =

            defendantAccountRepository
                .findById(Long.valueOf(requestNote.getRecordId()))
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Account %s not found".formatted(requestNote.getRecordId())
                ));

        if (!account.getVersion().equals(version)) {
            throw new ResponseStatusException(
            HttpStatus.PRECONDITION_FAILED,
            "Version mismatch. Expected " + account.getVersion() + " but got " + version);
        }

        NoteEntity note = new NoteEntity();

        note.setNoteText(requestNote.getNoteText());
        note.setNoteType(requestNote.getNoteType());
        note.setAssociatedRecordId(requestNote.getRecordId());
        note.setAssociatedRecordType(requestNote.getRecordType().toString());

        NoteEntity entity = repository.save(note);

        em.lock(account, LockModeType.OPTIMISTIC_FORCE_INCREMENT);

        return entity.getNoteId().toString();
    }
}
