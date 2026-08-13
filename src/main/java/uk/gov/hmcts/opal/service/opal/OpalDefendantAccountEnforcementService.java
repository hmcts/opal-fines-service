package uk.gov.hmcts.opal.service.opal;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldRequest;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldResponse;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.generated.model.AddEnforcementRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.AddEnforcementResponseDefendantAccount;
import uk.gov.hmcts.opal.generated.model.EnforcementInstalmentPeriodCommonStrict;
import uk.gov.hmcts.opal.generated.model.EnforcementPaymentTermsCommonStrict;
import uk.gov.hmcts.opal.generated.model.EnforcementPostedDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.EnforcementResultResponseDefendantAccount;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.dto.common.EnforcementOverride;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPaymentTermsRequest;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.service.AccountNoteContext;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.service.iface.DefendantAccountEnforcementServiceInterface;
import uk.gov.hmcts.opal.service.persistence.DebtorDetailRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.service.persistence.EnforcementRepositoryService;
import uk.gov.hmcts.opal.service.persistence.EnforcerRepositoryService;
import uk.gov.hmcts.opal.service.persistence.LocalJusticeAreaRepositoryService;
import uk.gov.hmcts.opal.service.persistence.ResultRepositoryService;
import uk.gov.hmcts.opal.service.proxy.NotesProxy;
import uk.gov.hmcts.opal.util.VersionUtils;

import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildEnforcementAction;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildEnforcementOverrideResult;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildEnforcementStatus;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.filterDefendantParty;

@Service
@Slf4j(topic = "opal.OpalDefendantAccountService")
@RequiredArgsConstructor
public class OpalDefendantAccountEnforcementService
    implements DefendantAccountEnforcementServiceInterface {

    private final DefendantAccountRepositoryService defendantAccountRepositoryService;

    private final LocalJusticeAreaRepositoryService localJusticeAreaRepositoryService;

    private final EnforcerRepositoryService enforcerRepositoryService;

    private final EnforcementRepositoryService enforcementRepositoryService;

    private final DebtorDetailRepositoryService debtorDetailRepositoryService;

    private final ResultRepositoryService resultRepositoryService;

    private final NotesProxy notesProxy;

    private final UserStateService userStateService;

    private final AmendmentService amendmentService;

    private final Clock clock;

    private final OpalDefendantAccountPaymentTermsService defendantAccountPaymentTermsService;

    private final ObjectMapper objectMapper;

    private final DefendantAccountControlValidator defendantAccountControlValidator;

    @Override
    @Transactional
    public AddEnforcementResponseDefendantAccount addEnforcement(
        Long defendantAccountId,
        Short businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        AddEnforcementRequestDefendantAccount request) throws JacksonException {

        String reason = null;
        Integer jailDays = null;
        Long enforcerId = null;
        LocalDateTime earliestReleaseDate = null;
        List<EnforcementResultResponseDefendantAccount> enforcementResultResponses = request != null
            && request.getEnforcementResultResponses() != null ? request.getEnforcementResultResponses() : List.of();

        for (EnforcementResultResponseDefendantAccount result : enforcementResultResponses) {
            if (Objects.equals(result.getParameterName(), "reason")) {
                reason = result.getResponse();
            }
            if (Objects.equals(result.getParameterName(), "jail_days")) {
                jailDays = Integer.valueOf(result.getResponse());
            }
            if (Objects.equals(result.getParameterName(), "enforcer_id")) {
                enforcerId = Long.valueOf(result.getResponse());
            }
            if (Objects.equals(result.getParameterName(), "earliest_release_date")) {
                earliestReleaseDate = LocalDateTime.parse(result.getResponse());
            }
        }

        String resultResponses = objectMapper.writeValueAsString(toResultResponsesMap(enforcementResultResponses));

        UserState userState = userStateService.getUserStateV1FromSecurityContext();
        DefendantAccountEntity defendant = defendantAccountRepositoryService.findById(defendantAccountId);

        Long enforcementId = enforcementRepositoryService.addDefendantAccountEnforcement(
            request.getResultId().toString(),
            defendantAccountId,
            businessUnitId,
            defendant.getProsecutorCaseReference(),
            "ACCOUNT_ENQUIRY",
            jailDays,
            businessUnitUserId,
            userState.getUserName(),
            reason,
            enforcerId,
            resultResponses,
            earliestReleaseDate,
            VersionUtils.extractBigInteger(ifMatch).longValue()
        );

        // The stored procedure updates defendant_accounts outside Hibernate. Refresh the managed account so chained
        // payment terms and the response use the latest version and enforcement state.
        defendantAccountRepositoryService.refresh(defendant);

        EnforcementPaymentTermsCommonStrict enforcementPaymentTerms = request.getPaymentTerms().orElse(null);
        if (enforcementPaymentTerms != null) {
            DefendantAccountEntity defendantEntity = defendantAccountRepositoryService.findById(defendantAccountId);
            defendantAccountPaymentTermsService.addPaymentTermsPreservingLastEnforcement(
                defendantAccountId,
                businessUnitId.toString(),
                businessUnitUserId,
                userState.getUserName(),
                defendantEntity.getVersion().toString(),
                AddDefendantAccountPaymentTermsRequest.builder()
                    .paymentTerms(toPaymentTerms(enforcementPaymentTerms))
                    .requestPaymentCard(false)
                    .generatePaymentTermsChangeLetter(false)
                    .build()
            );
        }

        DefendantAccountEntity latestDefendant = defendantAccountRepositoryService.findById(defendantAccountId);

        return AddEnforcementResponseDefendantAccount.builder()
            .defendantAccountId(String.valueOf(defendantAccountId))
            .version(Math.toIntExact(latestDefendant.getVersionNumber()))
            .enforcementId(String.valueOf(enforcementId))
            .build();
    }

    private Map<String, String> toResultResponsesMap(List<EnforcementResultResponseDefendantAccount> responses) {
        Map<String, String> resultResponsesMap = new LinkedHashMap<>();
        if (responses == null) {
            return resultResponsesMap;
        }

        for (EnforcementResultResponseDefendantAccount response : responses) {
            if (response == null || response.getParameterName() == null) {
                continue;
            }
            resultResponsesMap.put(response.getParameterName(), response.getResponse());
        }

        return resultResponsesMap;
    }

    private PaymentTerms toPaymentTerms(EnforcementPaymentTermsCommonStrict source) {
        if (source == null) {
            return null;
        }
        PaymentTerms paymentTerms = new PaymentTerms();
        paymentTerms.setDaysInDefault(source.getDaysInDefault().orElse(null));
        paymentTerms.setDateDaysInDefaultImposed(source.getDateDaysInDefaultImposed().orElse(null));
        paymentTerms.setExtension(Boolean.TRUE.equals(source.getExtension()));
        paymentTerms.setReasonForExtension(source.getReasonForExtension().orElse(null));
        paymentTerms.setEffectiveDate(source.getEffectiveDate().orElse(null));
        paymentTerms.setLumpSumAmount(source.getLumpSumAmount().orElse(null));
        paymentTerms.setInstalmentAmount(source.getInstalmentAmount().orElse(null));
        if (source.getPaymentTermsType() != null) {
            paymentTerms.setPaymentTermsType(PaymentTermsType.builder()
                .paymentTermsTypeCode(PaymentTermsType.PaymentTermsTypeCode.fromValue(
                    source.getPaymentTermsType().getPaymentTermsTypeCode().getValue()))
                .build());
        }
        EnforcementInstalmentPeriodCommonStrict instalmentPeriod = source.getInstalmentPeriod().orElse(null);
        if (instalmentPeriod != null) {
            paymentTerms.setInstalmentPeriod(InstalmentPeriod.builder()
                .instalmentPeriodCode(InstalmentPeriod.InstalmentPeriodCode.fromValue(
                    instalmentPeriod.getInstalmentPeriodCode().getValue()))
                .build());
        }
        EnforcementPostedDetailsCommonStrict postedDetails = source.getPostedDetails().orElse(null);
        if (postedDetails != null) {
            PostedDetails details = new PostedDetails();
            details.setPostedDate(postedDetails.getPostedDate());
            details.setPostedBy(postedDetails.getPostedBy().orElse(null));
            details.setPostedByName(postedDetails.getPostedByName().orElse(null));
            paymentTerms.setPostedDetails(details);
        }
        return paymentTerms;
    }

    @Override
    @Transactional
    public RemoveDefendantAccountEnforcementHoldResponse removeEnforcementHold(
        Long defendantAccountId,
        Short businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        RemoveDefendantAccountEnforcementHoldRequest request) {

        log.debug(":removeEnforcementHold: defendantAccountId={}, businessUnitId={}",
            defendantAccountId, businessUnitId);

        final UserState userState = userStateService.getUserStateV1FromSecurityContext();
        DefendantAccountEntity defendantEntity = defendantAccountRepositoryService.findById(defendantAccountId);

        if (ifMatch == null || ifMatch.isBlank()) {
            throw new ResourceConflictException(
                "Defendant Account",
                defendantAccountId,
                "If-Match header is required",
                defendantEntity
            );
        }

        VersionUtils.verifyIfMatch(defendantEntity, ifMatch, defendantAccountId, "removeEnforcementHold");
        defendantAccountControlValidator.validateCanRemoveEnforcementHold(defendantEntity);

        if (defendantEntity.getLastEnforcement() == null) {
            throw new ResourceConflictException(
                "Defendant Account",
                defendantAccountId,
                "No enforcement hold to remove",
                defendantEntity
            );
        }

        amendmentService.auditInitialiseStoredProc(
            defendantAccountId,
            RecordType.DEFENDANT_ACCOUNTS
        );

        defendantEntity.setLastEnforcement(null);
        defendantEntity.setLastMovementDate(LocalDate.now(clock));

        DefendantAccountEntity savedEntity = defendantAccountRepositoryService.saveAndFlush(defendantEntity);

        notesProxy.addNote(
            buildRemoveEnforcementHoldNoteRequest(defendantAccountId, request),
            VersionUtils.createETag(savedEntity),
            userState,
            new AccountNoteContext(
                DefendantAccountEntity.class,
                savedEntity.getDefendantAccountId(),
                businessUnitId,
                AssociatedRecordType.DEFENDANT_ACCOUNTS
            )
        );

        amendmentService.auditFinaliseStoredProc(
            defendantAccountId,
            RecordType.DEFENDANT_ACCOUNTS,
            businessUnitId,
            businessUnitUserId,
            userState.getUserName(),
            null,
            "Remove Enforcement Hold"
        );

        return RemoveDefendantAccountEnforcementHoldResponse.builder()
            .defendantAccountId(String.valueOf(savedEntity.getDefendantAccountId()))
            .version(savedEntity.getVersion())
            .build();
    }

    private AddNoteRequest buildRemoveEnforcementHoldNoteRequest(
        Long defendantAccountId,
        RemoveDefendantAccountEnforcementHoldRequest request) {

        Note note = Note.builder()
            .recordType(RecordType.DEFENDANT_ACCOUNTS)
            .recordId(String.valueOf(defendantAccountId))
            .noteText(request.getReason())
            .noteType("AA")
            .build();

        return new AddNoteRequest(note);
    }

    EnforcementOverride buildEnforcementOverride(DefendantAccountEntity entity) {
        if (entity.getEnforcementOverrideResultId() == null
            && entity.getEnforcementOverrideEnforcerId() == null
            && entity.getEnforcementOverrideTfoLjaId() == null) {
            return null;
        } else {
            return EnforcementOverride.builder()
                .enforcementOverrideResult(
                    buildEnforcementOverrideResult(
                        resultRepositoryService.getResultById(entity.getEnforcementOverrideResultId()).orElse(null)))
                .enforcer(OpalDefendantAccountBuilders.buildEnforcer(
                    enforcerRepositoryService.findById(entity.getEnforcementOverrideEnforcerId()).orElse(null)))
                .lja(OpalDefendantAccountBuilders.buildLja(
                    localJusticeAreaRepositoryService.getLjaById(entity.getEnforcementOverrideTfoLjaId()).orElse(null)))
                .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EnforcementStatus getEnforcementStatus(Long defendantAccountId) {

        log.debug(":getEnforcementStatus: def acc: {}", defendantAccountId);

        DefendantAccountEntity defendantEntity = defendantAccountRepositoryService
            .findById(defendantAccountId);
        DefendantAccountPartiesEntity defendantParty = filterDefendantParty(defendantEntity);
        EnforcementEntity recentEnforcement =
            enforcementRepositoryService.getEnforcementMostRecent(
                defendantEntity.getDefendantAccountId(), defendantEntity.getLastEnforcement()).orElse(null);

        return buildEnforcementStatus(
            defendantEntity,
            defendantParty,
            debtorDetailRepositoryService.findByPartyId(defendantParty.getParty().getPartyId()).orElse(null),
            recentEnforcement != null ? recentEnforcement.getResult() : null,
            buildEnforcementOverride(defendantEntity),
            buildEnforcementAction(
                recentEnforcement,
                recentEnforcement != null
                    ? enforcerRepositoryService.findById(recentEnforcement.getEnforcerId()).orElse(null)
                    : null));
    }
}
