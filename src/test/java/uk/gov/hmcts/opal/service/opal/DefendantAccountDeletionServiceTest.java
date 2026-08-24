package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.repository.AccountTransferRepository;
import uk.gov.hmcts.opal.repository.AllocationRepository;
import uk.gov.hmcts.opal.repository.AmendmentRepository;
import uk.gov.hmcts.opal.repository.BacsPaymentRepository;
import uk.gov.hmcts.opal.repository.ChequeRepository;
import uk.gov.hmcts.opal.repository.CommittalWarrantProgressRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountPartiesRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.DefendantTransactionRepository;
import uk.gov.hmcts.opal.repository.DocumentInstanceRepository;
import uk.gov.hmcts.opal.repository.EnforcementRepository;
import uk.gov.hmcts.opal.repository.FixedPenaltyOffenceRepository;
import uk.gov.hmcts.opal.repository.ImpositionRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.PaymentCardRequestRepository;
import uk.gov.hmcts.opal.repository.PaymentTermsRepository;
import uk.gov.hmcts.opal.repository.ReportEntryRepository;
import uk.gov.hmcts.opal.repository.ReportRepository;
import uk.gov.hmcts.opal.service.opal.jpa.CreditorAccountTransactional;

@ExtendWith(MockitoExtension.class)
class DefendantAccountDeletionServiceTest {

    @Mock
    private DefendantAccountRepository defendantAccountRepository;

    @Mock
    private DefendantAccountPartiesRepository defendantAccountPartiesRepository;

    @Mock
    private DefendantTransactionRepository defendantTransactionRepository;

    @Mock
    private PaymentTermsRepository paymentTermsRepository;

    @Mock
    private FixedPenaltyOffenceRepository fixedPenaltyOffenceRepository;

    @Mock
    private AccountTransferRepository accountTransferRepository;

    @Mock
    private EnforcementRepository enforcementRepository;

    @Mock
    private CommittalWarrantProgressRepository committalWarrantProgressRepository;

    @Mock
    private DocumentInstanceRepository documentInstanceRepository;

    @Mock
    private CreditorAccountTransactional creditorAccountTransactional;

    @Mock
    private PaymentCardRequestRepository paymentCardRequestsRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private ReportEntryRepository reportEntryRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ImpositionRepository impositionRepository;

    @Mock
    private AllocationRepository allocationsRepository;

    @Mock
    private ChequeRepository chequeRepository;

    @Mock
    private BacsPaymentRepository bacsPaymentsRepository;

    @Mock
    private AmendmentRepository amendmentRepository;

    @InjectMocks
    private DefendantAccountDeletionService service;

    @Test
    void deleteDefendantAccountAndAssociatedData_deletesDocumentInstancesAfterAccountTransfers() {
        long defendantAccountId = 1001L;
        when(defendantAccountRepository.existsById(defendantAccountId)).thenReturn(true);
        when(impositionRepository.findImpositionIdsByDefendantAccountId(defendantAccountId))
            .thenReturn(List.of(2001L, 2002L));
        when(defendantTransactionRepository.findDefendantAccountTransactionIdsByDefendantAccountId(defendantAccountId))
            .thenReturn(List.of());

        service.deleteDefendantAccountAndAssociatedData(defendantAccountId);

        InOrder inOrder = inOrder(
            accountTransferRepository,
            documentInstanceRepository,
            creditorAccountTransactional,
            defendantAccountRepository
        );
        inOrder.verify(accountTransferRepository).deleteByDefendantAccount_DefendantAccountId(defendantAccountId);
        inOrder.verify(documentInstanceRepository).deleteByAssociatedRecordTypeAndAssociatedRecordId(
            AssociatedRecordType.DEFENDANT_ACCOUNTS.getLabel(),
            String.valueOf(defendantAccountId)
        );
        inOrder.verify(documentInstanceRepository).deleteByAssociatedRecordTypeAndAssociatedRecordId(
            AssociatedRecordType.IMPOSITIONS.getLabel(),
            "2001"
        );
        inOrder.verify(documentInstanceRepository).deleteByAssociatedRecordTypeAndAssociatedRecordId(
            AssociatedRecordType.IMPOSITIONS.getLabel(),
            "2002"
        );
        inOrder.verify(creditorAccountTransactional)
            .deleteAllByDefendantAccountId(defendantAccountId, creditorAccountTransactional);
        inOrder.verify(defendantAccountRepository).deleteById(defendantAccountId);
    }

    @Test
    void deleteDefendantAccountAndAssociatedData_whenAccountMissing_doesNotDeleteDocumentInstances() {
        long defendantAccountId = 1001L;
        when(defendantAccountRepository.existsById(defendantAccountId)).thenReturn(false);

        assertThrows(
            EntityNotFoundException.class,
            () -> service.deleteDefendantAccountAndAssociatedData(defendantAccountId)
        );

        verifyNoInteractions(documentInstanceRepository, impositionRepository);
    }
}
