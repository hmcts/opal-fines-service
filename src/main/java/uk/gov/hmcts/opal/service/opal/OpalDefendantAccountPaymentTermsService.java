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
import uk.gov.hmcts.opal.service.iface.DefendantAccountPaymentTermsServiceInterface;
import uk.gov.hmcts.opal.service.iface.ReportEntryServiceInterface;
import uk.gov.hmcts.opal.service.persistence.AmendmentRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.service.persistence.EnforcementRepositoryService;
import uk.gov.hmcts.opal.service.persistence.PaymentCardRequestRepositoryService;
import uk.gov.hmcts.opal.service.persistence.PaymentTermsRepositoryService;
import uk.gov.hmcts.opal.service.persistence.ResultRepositoryService;
import uk.gov.hmcts.opal.util.VersionUtils;
import uk.gov.hmcts.opal.service.UserStateService;

import java.time.LocalDate;

@Service
@Slf4j(topic = "opal.OpalDefendantAccountService")
@RequiredArgsConstructor
public class OpalDefendantAccountPaymentTermsService implements DefendantAccountPaymentTermsServiceInterface {

    public static final String ACCOUNT_ENQUIRY = "ACCOUNT_ENQUIRY";
    private final DefendantAccountRepositoryService defendantAccountRepositoryService;

    private final PaymentTermsRepositoryService paymentTermsRepositoryService;

    private final AmendmentRepositoryService amendmentRepositoryService;

    private final PaymentCardRequestRepositoryService paymentCardRequestRepositoryService;

    private final UserStateService userStateService;

    private final AmendmentService amendmentService;

    private final DocumentService documentService;

    private final PaymentTermsService paymentTermsService;

    private final ReportEntryServiceInterface reportEntryService;

    private final PaymentTermsMapper paymentTermsMapper;

    private final ResultRepositoryService resultRepositoryService;
    private final EnforcementRepositoryService enforcementRepositoryService;

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
        String ifMatch) {

        log.debug(":addPaymentCardRequest (Opal): accountId={}, bu={}", defendantAccountId, businessUnitId);

        DefendantAccountEntity account = loadAndValidateAccount(defendantAccountId, businessUnitId);
        VersionUtils.verifyIfMatch(account, ifMatch, account.getDefendantAccountId(), "addPaymentCardRequest");

        amendmentRepositoryService.auditInitialiseStoredProc(defendantAccountId, RecordType.DEFENDANT_ACCOUNTS);

        ensureNoExistingPaymentCardRequest(defendantAccountId);

        createPaymentCardRequest(defendantAccountId);

        String displayName = userStateService.getUserStateV1FromSecurityContext().getDisplayName();
        updateDefendantAccountWithPcr(account, businessUnitUserId, displayName);

        auditComplete(defendantAccountId, account, businessUnitUserId, displayName);

        return new AddPaymentCardRequestResponse(defendantAccountId);
    }

    @Transactional
    @Override
    public GetDefendantAccountPaymentTermsResponse addPaymentTerms(Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        AddDefendantAccountPaymentTermsRequest addPaymentTermsRequest) {
        log.debug(":addPaymentTerms (Opal): accountId={}, bu={}", defendantAccountId, businessUnitId);

        DefendantAccountEntity defAccount = prepareDefendantAccountForPaymentTerms(
            defendantAccountId, businessUnitId, ifMatch);
        final PaymentTermsEntity savedPaymentTerms = persistPaymentTerms(defAccount, businessUnitUserId,
            addPaymentTermsRequest);

        addPaymentTerm(defAccount, addPaymentTermsRequest);
        clearLastEnforcementAction(defAccount);
        defendantAccountRepositoryService.save(defAccount);

        if (Boolean.TRUE.equals(addPaymentTermsRequest.getRequestPaymentCard())) {
            log.debug(":addPaymentTerms: Request Payment Card flag is TRUE for account {}",
                defAccount.getDefendantAccountId());
            addPaymentCard(defendantAccountId, businessUnitId, businessUnitUserId, ifMatch,
                userStateService.getUserStateV1FromSecurityContext().getDisplayName());
        }

        // if generate_payment_terms_change_letter is true
        //   Create a Document Instances record for a Payment Terms Change Letter.
        if (Boolean.TRUE.equals(addPaymentTermsRequest.getGeneratePaymentTermsChangeLetter())) {
            log.debug(":addPaymentTerms: Generate Payment Terms Change Letter flag is TRUE for account {}",
                defAccount.getDefendantAccountId());
            documentService.createDocumentInstance(defendantAccountId,
                defAccount.getBusinessUnit().getBusinessUnitId());
        }

        // Create report entry for the Extension of Time to Pay report
        reportEntryService.createExtendTtpReportEntry(savedPaymentTerms.getPaymentTermsId(),
            defAccount.getBusinessUnit().getBusinessUnitId());

        log.debug(":addPaymentTerms: saved payment terms id={} for account {}",
            savedPaymentTerms.getPaymentTermsId(), defAccount.getDefendantAccountId());

        amendmentService.auditFinaliseStoredProc(
            defAccount.getDefendantAccountId(),
            RecordType.DEFENDANT_ACCOUNTS,
            Short.parseShort(businessUnitId),
            businessUnitUserId,
            savedPaymentTerms.getPostedByUsername(),
            defAccount.getProsecutorCaseReference(),
            ACCOUNT_ENQUIRY
        );

        return OpalDefendantAccountBuilders.buildPaymentTermsResponse(savedPaymentTerms);
    }

    @Transactional
    public GetDefendantAccountPaymentTermsResponse addPaymentTermsPreservingLastEnforcement(Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        String authHeader,
        AddDefendantAccountPaymentTermsRequest addPaymentTermsRequest) {

        log.debug(":addPaymentTermsPreservingLastEnforcement (Opal): accountId={}, bu={}",
            defendantAccountId, businessUnitId);

        DefendantAccountEntity defAccount = prepareDefendantAccountForPaymentTerms(
            defendantAccountId, businessUnitId, ifMatch);
        final PaymentTermsEntity savedPaymentTerms = persistPaymentTerms(defAccount, businessUnitUserId,
            addPaymentTermsRequest);

        addPaymentTerm(defAccount, addPaymentTermsRequest);
        defendantAccountRepositoryService.save(defAccount);

        if (Boolean.TRUE.equals(addPaymentTermsRequest.getRequestPaymentCard())) {
            log.debug(":addPaymentTermsPreservingLastEnforcement: Request Payment Card flag is TRUE for account {}",
                defAccount.getDefendantAccountId());
            addPaymentCard(defendantAccountId, businessUnitId, businessUnitUserId, ifMatch, authHeader);
        }

        if (Boolean.TRUE.equals(addPaymentTermsRequest.getGeneratePaymentTermsChangeLetter())) {
            log.debug(":addPaymentTermsPreservingLastEnforcement: Generate Payment Terms Change Letter flag is TRUE "
                    + "for account {}",
                defAccount.getDefendantAccountId());
            documentService.createDocumentInstance(defendantAccountId,
                defAccount.getBusinessUnit().getBusinessUnitId());
        }

        reportEntryService.createExtendTtpReportEntry(savedPaymentTerms.getPaymentTermsId(),
            defAccount.getBusinessUnit().getBusinessUnitId());

        log.debug(":addPaymentTermsPreservingLastEnforcement: saved payment terms id={} for account {}",
            savedPaymentTerms.getPaymentTermsId(), defAccount.getDefendantAccountId());

        amendmentService.auditFinaliseStoredProc(
            defAccount.getDefendantAccountId(),
            RecordType.DEFENDANT_ACCOUNTS,
            Short.parseShort(businessUnitId),
            businessUnitUserId,
            savedPaymentTerms.getPostedByUsername(),
            defAccount.getProsecutorCaseReference(),
            ACCOUNT_ENQUIRY
        );

        return OpalDefendantAccountBuilders.buildPaymentTermsResponse(savedPaymentTerms);
    }

    private DefendantAccountEntity prepareDefendantAccountForPaymentTerms(Long defendantAccountId,
        String businessUnitId,
        String ifMatch) {

        DefendantAccountEntity defAccount = defendantAccountRepositoryService.findByIdForUpdate(defendantAccountId);
        validateBusinessUnit(defAccount, businessUnitId);
        VersionUtils.verifyIfMatch(defAccount, ifMatch, defendantAccountId, "addPaymentTerms");
        amendmentService.auditInitialiseStoredProc(defendantAccountId, RecordType.DEFENDANT_ACCOUNTS);
        paymentTermsService.deactivateExistingActivePaymentTerms(defAccount.getDefendantAccountId());
        return defAccount;
    }

    private PaymentTermsEntity persistPaymentTerms(DefendantAccountEntity defAccount,
        String businessUnitUserId,
        AddDefendantAccountPaymentTermsRequest addPaymentTermsRequest) {

        PaymentTermsEntity paymentTermsEntity = paymentTermsMapper.toEntity(addPaymentTermsRequest.getPaymentTerms());
        paymentTermsEntity.setDefendantAccount(defAccount);
        if (paymentTermsEntity.getPostedByUsername() == null) {
            paymentTermsEntity.setPostedByUsername(businessUnitUserId);
        }
        if (paymentTermsEntity.getPostedBy() == null) {
            paymentTermsEntity.setPostedBy(businessUnitUserId);
        }

        return paymentTermsService.addPaymentTerm(paymentTermsEntity);
    }

    private void validateBusinessUnit(DefendantAccountEntity defAccount, String businessUnitId) {
        if (defAccount.getBusinessUnit() == null
            || defAccount.getBusinessUnit().getBusinessUnitId() == null
            || !String.valueOf(defAccount.getBusinessUnit().getBusinessUnitId()).equals(businessUnitId)) {
            throw new EntityNotFoundException("Defendant Account not found in business unit " + businessUnitId);
        }
    }

    /**
     * Along with adding a new latest payment_terms we will also clear the last enforcement action on the account.
     * The only time it doesn't is when the last enforcement has result.extend_ttp_preserve_last_enf = TRUE
     */
    private void clearLastEnforcementAction(DefendantAccountEntity defAccount) {
        // Retrieve most recent enforcement action for this account using the result
        EnforcementEntity mostRecentEnforcement = fetchEnforcementMostRecent(defAccount);

        if (mostRecentEnforcement != null) {
            ResultEntity resultEntityLite = resultRepositoryService.getResultById(mostRecentEnforcement.getResultId())
                .orElseThrow(() -> new EntityNotFoundException("'Result' not found with id: "
                    + mostRecentEnforcement.getResultId()));

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
        return enforcementRepositoryService.getEnforcementMostRecent(
            entity.getDefendantAccountId(), entity.getLastEnforcement()).orElse(null);
    }

    /**
     * Add payment term related attributes to the defendant account.
     */
    private void addPaymentTerm(DefendantAccountEntity defAccount,
        AddDefendantAccountPaymentTermsRequest paymentTermsRequest) {

        defAccount.setSuspendedCommittalDate(paymentTermsRequest.getPaymentTerms().getDateDaysInDefaultImposed());
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
        String displayName) {

        log.debug(":addPaymentCard (Opal): accountId={}, bu={}", defendantAccountId, businessUnitId);

        DefendantAccountEntity account = loadAndValidateAccount(defendantAccountId, businessUnitId);
        VersionUtils.verifyIfMatch(account, ifMatch, account.getDefendantAccountId(), "addPaymentCard");

        ensureNoExistingPaymentCardRequest(defendantAccountId);

        createPaymentCardRequest(defendantAccountId);

        updateDefendantAccountWithPcr(account, businessUnitUserId, displayName);

        // Minimal response
        return new AddPaymentCardRequestResponse(defendantAccountId);
    }

    private DefendantAccountEntity loadAndValidateAccount(Long accountId, String buId) {
        DefendantAccountEntity account = defendantAccountRepositoryService.findById(accountId);
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
            ACCOUNT_ENQUIRY
        );
    }
}
