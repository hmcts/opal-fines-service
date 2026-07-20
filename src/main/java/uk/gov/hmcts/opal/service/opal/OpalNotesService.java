package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.util.VersionUtils.verifyIfMatch;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.NoteType;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.service.AccountNoteContext;
import uk.gov.hmcts.opal.service.iface.NotesServiceInterface;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.util.Versioned;

@Service
@Slf4j(topic = "opal.OpalNotesService")
@RequiredArgsConstructor
public class OpalNotesService implements NotesServiceInterface {

    private final NoteRepository repository;
    private final DefendantAccountRepositoryService defendantAccountRepositoryService;
    private final CreditorAccountRepository creditorAccountRepository;
    private final Clock clock;

    @Override
    @Transactional
    public String addNote(AddNoteRequest req, String ifMatch, UserState user, AccountNoteContext target) {
        log.info(":OpalAddNote");

        final Versioned account = getAccountAndVerifyVersion(target, ifMatch);

        Note requestNote = req.getActivityNote();

        NoteEntity note = new NoteEntity();
        note.setNoteText(requestNote.getNoteText());
        note.setNoteType(NoteType.valueOf(requestNote.getNoteType()));
        note.setAssociatedRecordId(requestNote.getRecordId());
        note.setAssociatedRecordType(target.associatedRecordType());
        note.setBusinessUnitUserId(target.businessUnitId().toString());
        note.setPostedDate(LocalDateTime.now(clock));
        note.setPostedByUsername(user.getDisplayName());

        NoteEntity entity = repository.save(note);

        return entity.getNoteId().toString();
    }

    private Versioned getAccountAndVerifyVersion(AccountNoteContext target, String ifMatch) {
        Versioned account = switch (target.associatedRecordType()) {
            case DEFENDANT_ACCOUNTS -> defendantAccountRepositoryService
                .getDefendantAccountByIdForUpdate(target.accountId());
            case CREDITOR_ACCOUNTS -> creditorAccountRepository.findByCreditorAccountIdForUpdate(target.accountId())
                .orElseThrow(() -> accountNotFound(target.accountId()));
            default -> throw new IllegalArgumentException(
                "Record type %s is not supported".formatted(target.associatedRecordType())
            );
        };

        verifyIfMatch(account, ifMatch, target.accountId(), "addNote");
        return account;
    }

    private EntityNotFoundException accountNotFound(Long accountId) {
        return new EntityNotFoundException("Account %s not found".formatted(accountId));
    }
}
