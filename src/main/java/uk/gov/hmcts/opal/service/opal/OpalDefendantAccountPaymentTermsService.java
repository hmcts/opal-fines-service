package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountSummaryViewEntity;
import uk.gov.hmcts.opal.entity.PaymentCardRequestEntity;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.repository.DefendantAccountPaymentTermsRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountSummaryViewRepository;
import uk.gov.hmcts.opal.repository.PaymentCardRequestRepository;
import uk.gov.hmcts.opal.service.iface.DefendantAccountPaymentTermsServiceInterface;
import uk.gov.hmcts.opal.util.VersionUtils;

import java.time.LocalDate;

@Service
@Slf4j(topic = "opal.OpalDefendantAccountService")
@RequiredArgsConstructor
public class OpalDefendantAccountPaymentTermsService implements DefendantAccountPaymentTermsServiceInterface {

    private final DefendantAccountRepository defendantAccountRepository;

    private final DefendantAccountPaymentTermsRepository defendantAccountPaymentTermsRepository;

    private final DefendantAccountSummaryViewRepository defendantAccountSummaryViewRepository;

    private final AmendmentService amendmentService;

    private final PaymentCardRequestRepository paymentCardRequestRepository;

    private final AccessTokenService accessTokenService;

    @Transactional(readOnly = true)
    public DefendantAccountEntity getDefendantAccountById(long defendantAccountId) {
        return defendantAccountRepository.findById(defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Defendant Account not found with id: " + defendantAccountId));
    }


    @Override
    @Transactional(readOnly = true)
    public GetDefendantAccountPaymentTermsResponse getPaymentTerms(Long defendantAccountId) {
        log.debug(":getPaymentTerms (Opal): criteria: {}", defendantAccountId);

        PaymentTermsEntity entity = defendantAccountPaymentTermsRepository
            .findTopByDefendantAccount_DefendantAccountIdOrderByPostedDateDescPaymentTermsIdDesc(
                defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException("Payment Terms not found for Defendant Account Id: "
                + defendantAccountId));

        return OpalDefendantAccountBuilders.buildPaymentTermsResponse(entity);
    }


    @Transactional(readOnly = true)
    public DefendantAccountSummaryViewEntity getDefendantAccountSummaryViewById(long defendantAccountId) {
        return defendantAccountSummaryViewRepository.findById(defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Defendant Account Summary View not found with id: " + defendantAccountId));
    }

    @Override
    @Transactional
    public AddPaymentCardRequestResponse addPaymentCardRequest(Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        String authHeader) {

        log.debug(":addPaymentCardRequest (Opal): accountId={}, bu={}", defendantAccountId, businessUnitId);

        DefendantAccountEntity account = loadAndValidateAccount(defendantAccountId, businessUnitId);
        VersionUtils.verifyIfMatch(account, ifMatch, account.getDefendantAccountId(), "addPaymentCardRequest");

        amendmentService.auditInitialiseStoredProc(defendantAccountId, RecordType.DEFENDANT_ACCOUNTS);

        ensureNoExistingPaymentCardRequest(defendantAccountId);

        createPaymentCardRequest(defendantAccountId);

        updateDefendantAccountWithPcr(account, businessUnitUserId, authHeader);

        auditComplete(defendantAccountId, account, businessUnitUserId);

        return new AddPaymentCardRequestResponse(defendantAccountId);
    }

    private DefendantAccountEntity loadAndValidateAccount(Long accountId, String buId) {
        DefendantAccountEntity account = getDefendantAccountById(accountId);
        validateBusinessUnitPresent(account, buId);
        return account;
    }

    private void validateBusinessUnitPresent(DefendantAccountEntity account, String buId) {
        if (account.getBusinessUnit() == null
            || account.getBusinessUnit().getBusinessUnitId() == null
            || !String.valueOf(account.getBusinessUnit().getBusinessUnitId()).equals(buId)) {
            throw new EntityNotFoundException("Defendant Account not found in business unit " + buId);
        }
    }

    private void ensureNoExistingPaymentCardRequest(Long accountId) {
        if (paymentCardRequestRepository.existsByDefendantAccountId(accountId)) {
            throw new ResourceConflictException(
                "DefendantAccountEntity",
                String.valueOf(accountId),
                "A payment card request already exists for this account.",
                null
            );
        }
    }

    private void createPaymentCardRequest(Long accountId) {
        PaymentCardRequestEntity pcr = PaymentCardRequestEntity.builder()
            .defendantAccountId(accountId)
            .build();
        paymentCardRequestRepository.save(pcr);
    }

    private void updateDefendantAccountWithPcr(DefendantAccountEntity account,
        String businessUnitUserId,
        String authHeader) {

        String displayName = accessTokenService.extractName(authHeader);

        account.setPaymentCardRequested(true);
        account.setPaymentCardRequestedDate(LocalDate.now());
        account.setPaymentCardRequestedBy(businessUnitUserId);
        account.setPaymentCardRequestedByName(displayName);

        defendantAccountRepository.save(account);
    }

    private void auditComplete(Long accountId,
        DefendantAccountEntity account,
        String businessUnitUserId) {

        Short buId = account.getBusinessUnit().getBusinessUnitId();

        amendmentService.auditFinaliseStoredProc(
            accountId,
            RecordType.DEFENDANT_ACCOUNTS,
            buId,
            businessUnitUserId,
            account.getProsecutorCaseReference(),
            "ACCOUNT_ENQUIRY"
        );
    }
}
