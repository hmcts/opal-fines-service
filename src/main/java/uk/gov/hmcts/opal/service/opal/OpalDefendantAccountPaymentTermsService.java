package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPaymentTermsRequest;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.PaymentCardRequestEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.controllers.advice.GlobalExceptionHandler.PaymentCardRequestAlreadyExistsException;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.mapper.request.PaymentTermsMapper;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.EnforcementRepository;
import uk.gov.hmcts.opal.service.iface.DefendantAccountPaymentTermsServiceInterface;
import uk.gov.hmcts.opal.service.persistence.AmendmentRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.service.persistence.PaymentCardRequestRepositoryService;
import uk.gov.hmcts.opal.service.persistence.PaymentTermsRepositoryService;
import uk.gov.hmcts.opal.util.VersionUtils;

import java.time.LocalDate;

@Service
@Slf4j(topic = "opal.OpalDefendantAccountService")
@RequiredArgsConstructor
public class OpalDefendantAccountPaymentTermsService implements DefendantAccountPaymentTermsServiceInterface {

    private final DefendantAccountRepository defendantAccountRepository;

    private final DefendantAccountRepositoryService defendantAccountRepositoryService;

    private final PaymentTermsRepositoryService paymentTermsRepositoryService;

    private final AmendmentRepositoryService amendmentRepositoryService;

    private final PaymentCardRequestRepositoryService paymentCardRequestRepositoryService;

    private final DefendantAccountControlValidator defendantAccountControlValidator;

    private final ReportEntryService reportEntryService;

    private final AmendmentService amendmentService;

    private final PaymentTermsMapper paymentTermsMapper;

    private final PaymentTermsService paymentTermsService;

    private final DocumentService documentService;

    private final ResultService resultService;

    private final EnforcementRepository enforcementRepository;

    @Override
    @Transactional(readOnly = true)
    public GetDefendantAccountPaymentTermsResponse getPaymentTerms(Long defendantAccountId) {
        log.debug(":getPaymentTerms (Opal): criteria: {}", defendantAccountId);

        PaymentTermsEntity entity = paymentTermsRepositoryService
            .findLatestByDefendantAccountId(
                defendantAccountId);

        return OpalDefendantAccountBuilders.buildPaymentTermsResponse(entity);
    }

    @Override
    @Transactional
    public AddPaymentCardRequestResponse addPaymentCardRequest(Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String postedByName,
        String ifMatch) {

        log.debug(":addPaymentCardRequest (Opal): accountId={}, bu={}", defendantAccountId, businessUnitId);

        DefendantAccountEntity account = loadAndValidateAccount(defendantAccountId, businessUnitId);
        VersionUtils.verifyIfMatch(account, ifMatch, account.getDefendantAccountId(), "addPaymentCardRequest");
        defendantAccountControlValidator.validateCanAddPaymentCardRequest(account);

        amendmentRepositoryService.auditInitialiseStoredProc(defendantAccountId, RecordType.DEFENDANT_ACCOUNTS);

        ensureNoExistingPaymentCardRequest(defendantAccountId);

        createPaymentCardRequest(defendantAccountId);

        updateDefendantAccountWithPcr(account, businessUnitUserId, postedByName);

        auditComplete(defendantAccountId, account, businessUnitUserId, postedByName);

        return new AddPaymentCardRequestResponse(defendantAccountId);
    }

    @Override
    @Transactional
    public GetDefendantAccountPaymentTermsResponse addPaymentTerms(Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String postedByName,
        String ifMatch,
        AddDefendantAccountPaymentTermsRequest addPaymentTermsRequest) {
        return addPaymentTermsInternal(
            defendantAccountId,
            businessUnitId,
            businessUnitUserId,
            postedByName,
            ifMatch,
            addPaymentTermsRequest,
            false
        );
    }

    @Transactional
    public GetDefendantAccountPaymentTermsResponse addPaymentTermsPreservingLastEnforcement(Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String postedByName,
        String ifMatch,
        AddDefendantAccountPaymentTermsRequest addPaymentTermsRequest) {
        return addPaymentTermsInternal(
            defendantAccountId,
            businessUnitId,
            businessUnitUserId,
            postedByName,
            ifMatch,
            addPaymentTermsRequest,
            true
        );
    }

    private GetDefendantAccountPaymentTermsResponse addPaymentTermsInternal(Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String postedByName,
        String ifMatch,
        AddDefendantAccountPaymentTermsRequest addPaymentTermsRequest,
        boolean preserveLastEnforcement) {

        log.debug(
            preserveLastEnforcement
                ? ":addPaymentTermsPreservingLastEnforcement (Opal): accountId={}, bu={}"
                : ":addPaymentTerms (Opal): accountId={}, bu={}",
            defendantAccountId,
            businessUnitId
        );

        DefendantAccountEntity defAccount = defendantAccountRepositoryService
            .getDefendantAccountByIdForUpdate(defendantAccountId);

        validateAccountExistsInBusinessUnit(defAccount, businessUnitId);
        VersionUtils.verifyIfMatch(defAccount, ifMatch, defendantAccountId, "addPaymentTerms");
        defendantAccountControlValidator.validateCanAddPaymentTerms(defAccount);

        amendmentService.auditInitialiseStoredProc(defendantAccountId, RecordType.DEFENDANT_ACCOUNTS);

        paymentTermsService.deactivateExistingActivePaymentTerms(defAccount.getDefendantAccountId());

        PaymentTermsEntity paymentTermsEntity = paymentTermsMapper.toEntity(addPaymentTermsRequest.getPaymentTerms());
        paymentTermsEntity.setDefendantAccount(defAccount);
        if (paymentTermsEntity.getPostedByUsername() == null) {
            paymentTermsEntity.setPostedByUsername(businessUnitUserId);
        }
        if (paymentTermsEntity.getPostedBy() == null) {
            paymentTermsEntity.setPostedBy(businessUnitUserId);
        }

        final PaymentTermsEntity savedPaymentTerms = paymentTermsService.addPaymentTerm(paymentTermsEntity);

        addPaymentTerm(defAccount, addPaymentTermsRequest);

        if (!preserveLastEnforcement) {
            clearLastEnforcementAction(defAccount);
        }

        defendantAccountRepository.save(defAccount);

        if (Boolean.TRUE.equals(addPaymentTermsRequest.getRequestPaymentCard())) {
            log.debug(
                preserveLastEnforcement
                    ? ":addPaymentTermsPreservingLastEnforcement: Request Payment Card flag is TRUE for account {}"
                    : ":addPaymentTerms: Request Payment Card flag is TRUE for account {}",
                defAccount.getDefendantAccountId()
            );
            addPaymentCard(defendantAccountId, businessUnitId, businessUnitUserId, ifMatch, postedByName, false);
        }

        if (Boolean.TRUE.equals(addPaymentTermsRequest.getGeneratePaymentTermsChangeLetter())) {
            log.debug(
                preserveLastEnforcement
                    ? ":addPaymentTermsPreservingLastEnforcement: Generate Payment Terms Change Letter flag is TRUE "
                        + "for account {}"
                    : ":addPaymentTerms: Generate Payment Terms Change Letter flag is TRUE for account {}",
                defAccount.getDefendantAccountId());
            documentService.createDocumentInstance(defendantAccountId,
                defAccount.getBusinessUnit().getBusinessUnitId());
        }

        Long reportEntryReferenceId = preserveLastEnforcement
            ? savedPaymentTerms.getPaymentTermsId()
            : defAccount.getDefendantAccountId();
        reportEntryService.createExtendTtpReportEntry(reportEntryReferenceId,
            defAccount.getBusinessUnit().getBusinessUnitId());

        log.debug(
            preserveLastEnforcement
                ? ":addPaymentTermsPreservingLastEnforcement: saved payment terms id={} for account {}"
                : ":addPaymentTerms: saved payment terms id={} for account {}",
            savedPaymentTerms.getPaymentTermsId(), defAccount.getDefendantAccountId());

        amendmentService.auditFinaliseStoredProc(
            defAccount.getDefendantAccountId(),
            RecordType.DEFENDANT_ACCOUNTS,
            Short.parseShort(businessUnitId),
            businessUnitUserId,
            postedByName,
            defAccount.getProsecutorCaseReference(),
            "ACCOUNT_ENQUIRY"
        );

        return OpalDefendantAccountBuilders.buildPaymentTermsResponse(savedPaymentTerms);
    }

    /**
     * Along with adding a new latest payment_terms we will also clear the last enforcement action on the account.
     * The only time it doesn't is when the last enforcement has result.extend_ttp_preserve_last_enf = TRUE
     */
    private void clearLastEnforcementAction(DefendantAccountEntity defAccount) {
        // Retrieve most recent enforcement action for this account using the result
        EnforcementEntity mostRecentEnforcement = fetchEnforcementMostRecent(defAccount);

        if (mostRecentEnforcement != null) {
            ResultEntity resultEntityLite = resultService.getResultById(mostRecentEnforcement.getResultId());

            // If resultEntity exists and extend_ttp_preserve_last_enf is not TRUE, clear last_enforcement
            if (!resultEntityLite.isExtendTtpPreserveLastEnf()) {
                log.debug(":clearLastEnforcementAction: Clearing last_enforcement={} for account {}",
                    defAccount.getLastEnforcement(), defAccount.getDefendantAccountId());
                defAccount.setLastEnforcement(null);

            } else {
                log.debug(":clearLastEnforcementAction: Preserving last_enforcement={} for account {} as "
                        + "extend_ttp_preserve_last_enf=TRUE",
                    defAccount.getLastEnforcement(), defAccount.getDefendantAccountId());
            }
        }
    }

    private EnforcementEntity fetchEnforcementMostRecent(DefendantAccountEntity entity) {
        return enforcementRepository.findFirstByDefendantAccountIdAndResultIdOrderByPostedDateDesc(
            entity.getDefendantAccountId(), entity.getLastEnforcement()).orElse(null);
    }

    /**
     * This method adds a payment card request to a defendant account.
     *
     * <p>
     * This method is separated from the public interface to allow for
     * addition of a payment card in a chained operation.
     * </p>
     */
    private AddPaymentCardRequestResponse addPaymentCard(
        Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        String displayName,
        boolean validatePaymentCardControls) {

        log.debug(":addPaymentCard (Opal): accountId={}, bu={}", defendantAccountId, businessUnitId);

        DefendantAccountEntity account = loadAndValidateAccount(defendantAccountId, businessUnitId);
        VersionUtils.verifyIfMatch(account, ifMatch, account.getDefendantAccountId(), "addPaymentCard");
        if (validatePaymentCardControls) {
            defendantAccountControlValidator.validateCanAddPaymentCardRequest(account);
        }

        ensureNoExistingPaymentCardRequest(defendantAccountId);

        createPaymentCardRequest(defendantAccountId);

        updateDefendantAccountWithPcr(account, businessUnitUserId, displayName);

        // Minimal response
        return new AddPaymentCardRequestResponse(defendantAccountId);
    }

    /**
     * Add payment term related attributes to the defendant account.
     */
    private void addPaymentTerm(DefendantAccountEntity defAccount,
        AddDefendantAccountPaymentTermsRequest paymentTermsRequest) {

        defAccount.setSuspendedCommittalDate(paymentTermsRequest.getPaymentTerms().getDateDaysInDefaultImposed());
    }

    private DefendantAccountEntity loadAndValidateAccount(Long accountId, String buId) {
        DefendantAccountEntity account = defendantAccountRepositoryService.findById(accountId);
        validateAccountExistsInBusinessUnit(account, buId);
        return account;
    }

    private void validateAccountExistsInBusinessUnit(DefendantAccountEntity account, String businessUnitId) {
        if (!account.isInBusinessUnit(businessUnitId)) {
            throw new EntityNotFoundException("Defendant Account not found in business unit " + businessUnitId);
        }
    }

    private void ensureNoExistingPaymentCardRequest(Long accountId) {
        if (paymentCardRequestRepositoryService.existsByDefendantAccountId(accountId)) {
            throw new PaymentCardRequestAlreadyExistsException(
                "DefendantAccountEntity",
                String.valueOf(accountId)
            );
        }
    }

    private void createPaymentCardRequest(Long accountId) {
        PaymentCardRequestEntity pcr = PaymentCardRequestEntity.builder()
            .defendantAccountId(accountId)
            .build();
        paymentCardRequestRepositoryService.save(pcr);
    }

    private void updateDefendantAccountWithPcr(DefendantAccountEntity account,
        String businessUnitUserId,
        String displayName) {

        account.setPaymentCardRequested(true);
        account.setPaymentCardRequestedDate(LocalDate.now());
        account.setPaymentCardRequestedBy(businessUnitUserId);
        account.setPaymentCardRequestedByName(displayName);

        defendantAccountRepositoryService.save(account);
    }

    private void auditComplete(Long accountId,
        DefendantAccountEntity account,
        String businessUnitUserId,
        String postedByName) {

        Short buId = account.getBusinessUnit().getBusinessUnitId();

        amendmentRepositoryService.auditFinaliseStoredProc(
            accountId,
            RecordType.DEFENDANT_ACCOUNTS,
            buId,
            businessUnitUserId,
            postedByName,
            account.getProsecutorCaseReference(),
            "ACCOUNT_ENQUIRY"
        );
    }
}
