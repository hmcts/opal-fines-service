package uk.gov.hmcts.opal.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.service.proxy.NotesProxy;

@Service
@Slf4j(topic = "opal.NotesService")
@RequiredArgsConstructor
public class NotesService {

    private final NotesProxy notesProxy;
    private final UserStateService userStateService;
    private final DefendantAccountRepository defendantAccountRepository;
    private final CreditorAccountRepository creditorAccountRepository;

    public String addNote(AddNoteRequest request, String ifMatch, Short businessUnitId) {
        log.debug(":addNote:");

        UserState userState = userStateService.getUserStateV1FromSecurityContext();
        AccountNoteContext target = getAccountNoteContext(request.getActivityNote());

        if (!userState.hasBusinessUnitUserWithPermission(
            businessUnitId, FinesPermission.ADD_ACCOUNT_ACTIVITY_NOTES)) {
            throw new PermissionNotAllowedException(businessUnitId, FinesPermission.ADD_ACCOUNT_ACTIVITY_NOTES);
        }

        return notesProxy.addNote(request, ifMatch, userState, target);
    }

    private AccountNoteContext getAccountNoteContext(Note note) {
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
