package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;

@ExtendWith(MockitoExtension.class)
class NotesServiceAccountContextTest {

    @Mock private DefendantAccountRepository defendantAccountRepository;
    @Mock private CreditorAccountRepository creditorAccountRepository;

    @Test
    void from_forCreditorAccount_usesCreditorAccountContext() {
        final AccountNoteContextFactory factory = new AccountNoteContextFactory(
            defendantAccountRepository,
            creditorAccountRepository
        );

        final Note note = Note.builder()
            .recordType(RecordType.CREDITOR_ACCOUNTS)
            .recordId("104")
            .noteText("creditor note")
            .noteType("AA")
            .build();

        CreditorAccountEntity creditorAccount = new CreditorAccountEntity();
        creditorAccount.setCreditorAccountId(104L);
        creditorAccount.setBusinessUnitId((short) 10);

        when(creditorAccountRepository.findById(104L)).thenReturn(Optional.of(creditorAccount));

        AccountNoteContext target = factory.from(note);

        verify(defendantAccountRepository, never()).findById(104L);

        assertEquals(CreditorAccountEntity.class, target.accountClass());
        assertEquals(104L, target.accountId());
        assertEquals((short) 10, target.businessUnitId());
        assertEquals(AssociatedRecordType.CREDITOR_ACCOUNTS, target.associatedRecordType());
    }
}
