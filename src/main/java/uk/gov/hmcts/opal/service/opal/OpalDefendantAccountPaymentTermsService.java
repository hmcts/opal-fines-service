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
@Slf4j(topic = "opal.OpalDefendantAccountPaymentTermsService")
@RequiredArgsConstructor
public class OpalDefendantAccountPaymentTermsService implements DefendantAccountPaymentTermsServiceInterface {

    private final DefendantAccountRepositoryService defendantAccountRepositoryService;

    private final PaymentTermsRepositoryService paymentTermsRepositoryService;

    private final AmendmentRepositoryService amendmentRepositoryService;

    private final PaymentCardRequestRepositoryService paymentCardRequestRepositoryService;

    private final DocumentService documentService;

    private final PaymentTermsService paymentTermsService;

    private final AmendmentService amendmentService;

    private final ReportEntryService reportEntryService;

    private final PaymentTermsMapper paymentTermsMapper;

    private final DefendantAccountRepository defendantAccountRepository;

    private final EnforcementRepository enforcementRepository;

    private final ResultService resultService;

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

        amendmentRepositoryService.auditInitialiseStoredProc(defendantAccountId, RecordType.DEFENDANT_ACCOUNTS);

        AddPaymentCardRequestResponse response = addPaymentCard(
            defendantAccountId,
            businessUnitId,
            businessUnitUserId,
            ifMatch,
            postedByName,
            "addPaymentCardRequest"
        );

        DefendantAccountEntity account = loadAndValidateAccount(defendantAccountId, businessUnitId);
        auditComplete(defendantAccountId, account, businessUnitUserId, postedByName);

        return response;
    }

    @Override
    @Transactional
    public GetDefendantAccountPaymentTermsResponse addPaymentTerms(Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String postedByName,
        String ifMatch,
        AddDefendantAccountPaymentTermsRequest addPaymentTermsRequest) {
        return addPaymentTermsInternal(defendantAccountId, businessUnitId, businessUnitUserId, postedByName, ifMatch,
            addPaymentTermsRequest, false);
    }

    @Transactional
    public GetDefendantAccountPaymentTermsResponse addPaymentTermsPreservingLastEnforcement(Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String postedByName,
        String ifMatch,
        AddDefendantAccountPaymentTermsRequest addPaymentTermsRequest) {
        return addPaymentTermsInternal(defendantAccountId, businessUnitId, businessUnitUserId, postedByName, ifMatch,
            addPaymentTermsRequest, true);
    }


    private DefendantAccountEntity loadAndValidateAccount(Long accountId, String buId) {
        DefendantAccountEntity account = defendantAccountRepositoryService.findById(accountId);
        validateBusinessUnitPresent(account, buId);
        return account;
    }

    private DefendantAccountEntity loadAndValidateAccountForUpdate(Long accountId, String buId) {
        DefendantAccountEntity account = defendantAccountRepositoryService.getDefendantAccountByIdForUpdate(accountId);
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
            "ACCOUNT_ENQUIRY"
        );
    }

    private GetDefendantAccountPaymentTermsResponse addPaymentTermsInternal(Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String postedByName,
        String ifMatch,
        AddDefendantAccountPaymentTermsRequest addPaymentTermsRequest,
        boolean preserveLastEnforcement) {

        String operationName = preserveLastEnforcement
            ? "addPaymentTermsPreservingLastEnforcement"
            : "addPaymentTerms";
        log.debug(":{} (Opal): accountId={}, bu={}", operationName, defendantAccountId, businessUnitId);

        DefendantAccountEntity defAccount = loadAndValidateAccountForUpdate(defendantAccountId, businessUnitId);
        VersionUtils.verifyIfMatch(defAccount, ifMatch, defendantAccountId, operationName);

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
            log.debug(":{}: Request Payment Card flag is TRUE for account {}",
                operationName, defAccount.getDefendantAccountId());
            addPaymentCard(defendantAccountId, businessUnitId, businessUnitUserId, ifMatch, postedByName,
                "addPaymentCard");
        }

        if (Boolean.TRUE.equals(addPaymentTermsRequest.getGeneratePaymentTermsChangeLetter())) {
            log.debug(":{}: Generate Payment Terms Change Letter flag is TRUE for account {}",
                operationName, defAccount.getDefendantAccountId());
            documentService.createDocumentInstance(defendantAccountId,
                defAccount.getBusinessUnit().getBusinessUnitId());
        }

        reportEntryService.createExtendTtpReportEntry(
            preserveLastEnforcement ? savedPaymentTerms.getPaymentTermsId() : defAccount.getDefendantAccountId(),
            defAccount.getBusinessUnit().getBusinessUnitId());

        log.debug(":{}: saved payment terms id={} for account {}",
            operationName, savedPaymentTerms.getPaymentTermsId(), defAccount.getDefendantAccountId());

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
        String operationName) {

        log.debug(":{} (Opal): accountId={}, bu={}", operationName, defendantAccountId, businessUnitId);

        DefendantAccountEntity account = loadAndValidateAccount(defendantAccountId, businessUnitId);
        VersionUtils.verifyIfMatch(account, ifMatch, account.getDefendantAccountId(), operationName);

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


}
