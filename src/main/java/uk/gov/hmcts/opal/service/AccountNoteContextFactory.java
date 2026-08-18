package uk.gov.hmcts.opal.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;

@Component
@RequiredArgsConstructor
public class AccountNoteContextFactory {

    private final DefendantAccountRepository defendantAccountRepository;
    private final CreditorAccountRepository creditorAccountRepository;

    public AccountNoteContext from(Note note) {
        Long accountId = Long.valueOf(note.getRecordId());
        RecordType recordType = note.getRecordType();

        return switch (recordType) {
            case DEFENDANT_ACCOUNTS -> defendantAccountRepository
                .findById(accountId)
                .map(account -> new AccountNoteContext(
                    DefendantAccountEntity.class,
                    account.getDefendantAccountId(),
                    account.getBusinessUnit().getBusinessUnitId(),
                    AssociatedRecordType.DEFENDANT_ACCOUNTS
                ))
                .orElseThrow(() -> accountNotFound(note.getRecordId()));
            case CREDITOR_ACCOUNTS -> creditorAccountRepository
                .findById(accountId)
                .map(account -> new AccountNoteContext(
                    CreditorAccountEntity.class,
                    account.getCreditorAccountId(),
                    account.getBusinessUnitId(),
                    AssociatedRecordType.CREDITOR_ACCOUNTS
                ))
                .orElseThrow(() -> accountNotFound(note.getRecordId()));
            default -> throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Record type %s is not supported".formatted(recordType)
            );
        };
    }

    private EntityNotFoundException accountNotFound(String accountId) {
        return new EntityNotFoundException("Account %s not found".formatted(accountId));
    }
}
