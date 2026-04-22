package uk.gov.hmcts.opal.service.opal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldRequest;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldResponse;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.ResultResponse;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.dto.common.EnforcementOverride;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPaymentTermsRequest;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.iface.DefendantAccountEnforcementServiceInterface;
import uk.gov.hmcts.opal.service.persistence.DebtorDetailRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.service.persistence.EnforcementRepositoryService;
import uk.gov.hmcts.opal.service.persistence.EnforcerRepositoryService;
import uk.gov.hmcts.opal.service.persistence.LocalJusticeAreaRepositoryService;
import uk.gov.hmcts.opal.service.persistence.ResultRepositoryService;
import uk.gov.hmcts.opal.service.proxy.NotesProxy;
import uk.gov.hmcts.opal.util.VersionUtils;

import java.time.LocalDateTime;
import java.util.Objects;

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

    private final ReportEntryService reportEntryService;

    private final Clock clock;

    private final OpalDefendantAccountService opalDefendantAccountService;

    private final ObjectMapper objectMapper;

    @Override
    public AddEnforcementResponse addEnforcement(
        Long defendantAccountId,
        Short businessUnitId,
        String businessUnitUserId,
        Long ifMatch,
        String authHeader,
        AddDefendantAccountEnforcementRequest request) throws JsonProcessingException {

        UserState userState = userStateService.checkForAuthorisedUser(authHeader);
        DefendantAccountEntity defendant = defendantAccountRepositoryService.findById(defendantAccountId);

        String reason = null;
        Integer jailDays = null;
        Long enforcerId = null;
        LocalDateTime earliestReleaseDate = null;

        for (ResultResponse result : request.getEnforcementResultResponses()) {
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

        String resultResponses = objectMapper.writeValueAsString(request.getEnforcementResultResponses());

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
            ifMatch
        );

        if (request.getPaymentTerms() != null) {
            DefendantAccountEntity defendantEntity = defendantAccountRepositoryService.findById(defendantAccountId);
            opalDefendantAccountService.addPaymentTerms(defendantAccountId,
                                                        businessUnitId.toString(),
                                                        businessUnitUserId,
                                                        defendantEntity.getVersion().toString(),
                                                        authHeader,
                                                        AddDefendantAccountPaymentTermsRequest.builder()
                                                            .paymentTerms(request.getPaymentTerms())
                                                            .requestPaymentCard(false)
                                                            .generatePaymentTermsChangeLetter(false)
                                                            .build()
            );
        }

        return AddEnforcementResponse.builder()
            .defendantAccountId(String.valueOf(defendantAccountId))
            .version(Math.toIntExact(ifMatch))
            .enforcementId(String.valueOf(enforcementId))
            .build();
    }

    @Override
    @Transactional
    public RemoveDefendantAccountEnforcementHoldResponse removeEnforcementHold(
        Long defendantAccountId,
        Short businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        String authHeader,
        RemoveDefendantAccountEnforcementHoldRequest request) {

        log.debug(":removeEnforcementHold: defendantAccountId={}, businessUnitId={}",
            defendantAccountId, businessUnitId);

        final UserState userState = userStateService.checkForAuthorisedUser(authHeader);
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
            savedEntity
        );

        reportEntryService.createRemoveEnforcementHoldReportEntry(defendantAccountId, businessUnitId);

        amendmentService.auditFinaliseStoredProc(
            defendantAccountId,
            RecordType.DEFENDANT_ACCOUNTS,
            businessUnitId,
            businessUnitUserId,
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
