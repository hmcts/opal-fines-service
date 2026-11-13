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
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.SuspenseAccountEntity;
import uk.gov.hmcts.opal.entity.SuspenseItemEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.generated.model.NoteCommon;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.SuspenseItemRepository;

@ExtendWith(MockitoExtension.class)
class NotesServiceAccountContextTest {

    @Mock private DefendantAccountRepository defendantAccountRepository;
    @Mock private CreditorAccountRepository creditorAccountRepository;
    @Mock private SuspenseItemRepository suspenseItemRepository;

    @Test
    void from_forCreditorAccount_usesCreditorAccountContext() {
        final AccountNoteContextFactory factory = new AccountNoteContextFactory(
            defendantAccountRepository,
            creditorAccountRepository, suspenseItemRepository
        );

        final NoteCommon note = NoteCommon.builder()
            .recordType(NoteCommon.RecordTypeEnum.CREDITOR_ACCOUNTS)
            .recordId("104")
            .noteText("creditor note")
            .noteType(NoteCommon.NoteTypeEnum.AA)
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

    @Test
    void from_forSuspenseItem_usesBusinessUnitFromSuspenseAccount() {
        AccountNoteContextFactory factory = new AccountNoteContextFactory(
            defendantAccountRepository, creditorAccountRepository, suspenseItemRepository);
        NoteCommon note = NoteCommon.builder().recordId("105")
            .recordType(NoteCommon.RecordTypeEnum.SUSPENSE_ITEMS).build();
        SuspenseItemEntity item = SuspenseItemEntity.builder().suspenseItemId(105L)
            .suspenseAccount(SuspenseAccountEntity.builder().businessUnit(
                BusinessUnitEntity.builder().businessUnitId((short) 10).build()).build()).build();
        when(suspenseItemRepository.findBySuspenseItemId(105L)).thenReturn(Optional.of(item));

        AccountNoteContext target = factory.from(note);

        assertEquals(SuspenseItemEntity.class, target.accountClass());
        assertEquals(105L, target.accountId());
        assertEquals((short) 10, target.businessUnitId());
        assertEquals(AssociatedRecordType.SUSPENSE_ITEMS, target.associatedRecordType());
    }
}
