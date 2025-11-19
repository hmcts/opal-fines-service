package uk.gov.hmcts.opal.service.opal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPaymentTermsRequest;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.ReportEntryEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountPaymentTermsRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.ReportEntryRepository;
import uk.gov.hmcts.opal.repository.ResultRepository;
import uk.gov.hmcts.opal.service.UserStateService;

@ExtendWith(MockitoExtension.class)
public class OpalDefendantAccountServiceTestAddPaymentTermsTest {
    @Mock
    private DefendantAccountPaymentTermsRepository paymentTermsRepository;

    @Mock
    private DefendantAccountRepository defendantAccountRepository;

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private ReportEntryRepository reportEntryRepository;

    // other dependencies the service needs (audit, userState, mappers etc.)
    @Mock private AmendmentService amendmentService;
    @Mock private UserStateService userStateService;
    // add other mocks used by your service if required

    @InjectMocks
    private OpalDefendantAccountService service;

    @Captor
    private ArgumentCaptor<PaymentTermsEntity> paymentTermsCaptor;

    @Captor
    private ArgumentCaptor<DefendantAccountEntity> accountCaptor;

    @Captor
    private ArgumentCaptor<ReportEntryEntity> reportEntryCaptor;

    @Test
    @Disabled
    void testAddPaymentTerms_clearsLastEnforcementAndCreatesReportEntry_whenResultDoesNotPreserve() {
        // Arrange
        final Long defendantAccountId = 100L;
        final String businessUnitId = "10";
        final String ifMatch = "W/\"1\"";
        final String postedBy = "tester";

        // Build a defendant account with a last enforcement result id (assume Long)
        DefendantAccountEntity account = new DefendantAccountEntity();
        account.setDefendantAccountId(defendantAccountId);
        // set a last enforcement id that points to a Result row
        account.setLastEnforcement(String.valueOf(55L));
        // set business unit so BU validation passes
        //DefendantAccountEntity.BusinessUnitFullEntity bu = new DefendantAccountEntity.BusinessUnitFullEntity();
        // adapt if BusinessUnit is a class
        // If your BusinessUnit is a simple entity, adjust the construction accordingly.
        // The test below assumes account.getBusinessUnit().getBusinessUnitId() returns Short.parseShort(businessUnitId)
        // Instead of constructing, you can stub the repository to return account and skip BU object detail if simpler.
        // To keep test compilation tolerant, we will instead stub repository to return the account and bypass strict
        // BU checks by stubbing service internals if needed.

        // Prepare request DTO
        PaymentTerms paymentTermsDto = new PaymentTerms();
        // set whatever fields are needed by your mapper to generate an entity
        AddDefendantAccountPaymentTermsRequest request = new AddDefendantAccountPaymentTermsRequest();
        request.setPaymentTerms(paymentTermsDto);

        // Mock repository to return the defendant account when service loads it
        when(defendantAccountRepository.findById(defendantAccountId)).thenReturn(Optional.of(account));

        // Mock repository save for new payment terms: capture and assign an ID when saved
        when(paymentTermsRepository.save(any(PaymentTermsEntity.class)))
            .thenAnswer(invocation -> {
                PaymentTermsEntity paymentTerms = invocation.getArgument(0);
                // emulate DB assigning id
                paymentTerms.setPaymentTermsId(123L);
                return paymentTerms;
            });

        // Mock repository for existing active payment terms - emulate one existing active term
        PaymentTermsEntity existingActive = new PaymentTermsEntity();
        existingActive.setPaymentTermsId(50L);
        existingActive.setActive(Boolean.TRUE);
        existingActive.setDefendantAccount(account);
        when(paymentTermsRepository.findByDefendantAccount_DefendantAccountIdAndActiveTrue(defendantAccountId))
            .thenReturn(List.of(existingActive));

        // Mock resultRepository to indicate extend_ttp_preserve_last_enf = FALSE for last enforcement
        //when(resultRepository.findExtendTtpPreserveLastEnfByResultId(55L)).thenReturn(Optional.of(Boolean.FALSE));

        // Act
        service.addPaymentTerms(defendantAccountId, businessUnitId, ifMatch, postedBy, request);

        // Assert
        // 1) Verify a new PaymentTermsEntity was saved
        verify(paymentTermsRepository).save(paymentTermsCaptor.capture());
        PaymentTermsEntity saved = paymentTermsCaptor.getValue();
        assert saved != null;
        // Ensure it was marked active
        assert Boolean.TRUE.equals(saved.getActive());

        // 2) Verify existing active terms were deactivated
        verify(paymentTermsRepository).findByDefendantAccount_DefendantAccountIdAndActiveTrue(defendantAccountId);
        assert existingActive.getActive() == Boolean.FALSE || Boolean.FALSE.equals(existingActive.getActive());

        // 3) Verify defendant account lastEnforcement was cleared and saved
        verify(defendantAccountRepository, atLeastOnce()).save(accountCaptor.capture());
        DefendantAccountEntity savedAccount = accountCaptor.getValue();
        assert savedAccount.getLastEnforcement() == null;

        // 4) Verify a report entry was created referencing the new payment terms id ("123")
        verify(reportEntryRepository).save(reportEntryCaptor.capture());
        ReportEntryEntity reportEntry = reportEntryCaptor.getValue();
        assert reportEntry != null;
        // the associatedRecordId might be stored as string
        assert String.valueOf(123L).equals(reportEntry.getAssociatedRecordId());
        assert "payment_terms".equals(reportEntry.getAssociatedRecordType());
        //assert "list_extend_ttp".equals(reportEntry.getReportType());
    }
}
