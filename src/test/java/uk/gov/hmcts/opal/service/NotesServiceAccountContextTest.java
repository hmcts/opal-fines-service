package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.service.proxy.NotesProxy;

@ExtendWith(MockitoExtension.class)
class NotesServiceAccountContextTest {

    @Mock private NotesProxy notesProxy;
    @Mock private UserStateService userStateService;
    @Mock private DefendantAccountRepository defendantAccountRepository;
    @Mock private CreditorAccountRepository creditorAccountRepository;
    @Mock private UserState userState;

    @Test
    void addNote_forCreditorAccount_usesCreditorAccountContext() {
        final NotesService service = new NotesService(
            notesProxy,
            userStateService,
            defendantAccountRepository,
            creditorAccountRepository
        );

        final AddNoteRequest request = new AddNoteRequest(Note.builder()
            .recordType(RecordType.CREDITOR_ACCOUNTS)
            .recordId("104")
            .noteText("creditor note")
            .noteType("AA")
            .build());

        CreditorAccountEntity creditorAccount = new CreditorAccountEntity();
        creditorAccount.setCreditorAccountId(104L);
        creditorAccount.setBusinessUnitId((short) 10);

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(creditorAccountRepository.findById(104L)).thenReturn(Optional.of(creditorAccount));
        when(userState.hasBusinessUnitUserWithPermission((short) 10, FinesPermission.ADD_ACCOUNT_ACTIVITY_NOTES))
            .thenReturn(true);
        when(notesProxy.addNote(eq(request), eq("\"3\""), eq(userState), org.mockito.ArgumentMatchers.any()))
            .thenReturn("123");

        String noteId = service.addNote(request, "\"3\"", (short) 10);

        assertEquals("123", noteId);
        verify(defendantAccountRepository, never()).findById(104L);

        ArgumentCaptor<AccountNoteContext> targetCaptor = ArgumentCaptor.forClass(AccountNoteContext.class);
        verify(notesProxy).addNote(eq(request), eq("\"3\""), eq(userState), targetCaptor.capture());

        AccountNoteContext target = targetCaptor.getValue();
        assertEquals(CreditorAccountEntity.class, target.accountClass());
        assertEquals(104L, target.accountId());
        assertEquals((short) 10, target.businessUnitId());
        assertEquals(AssociatedRecordType.CREDITOR_ACCOUNTS, target.associatedRecordType());
    }
}
