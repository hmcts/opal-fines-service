package uk.gov.hmcts.opal.service.opal;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.generated.model.AddEnforcementRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.AddEnforcementResponseDefendantAccount;
import uk.gov.hmcts.opal.generated.model.AddPaymentTermsRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.EnforcementPaymentTermsCommonStrict;
import uk.gov.hmcts.opal.generated.model.EnforcementResultResponseDefendantAccount;
import uk.gov.hmcts.opal.generated.model.AddNoteRequestNotes;
import uk.gov.hmcts.opal.generated.model.NoteCommon;
import uk.gov.hmcts.opal.generated.model.RemoveEnforcementHoldRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.RemoveEnforcementHoldResponseDefendantAccount;
import uk.gov.hmcts.opal.dto.common.EnforcementOverride;
import uk.gov.hmcts.opal.mapper.EnforcementPaymentTermsMapper;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.repository.CourtRepository;
import uk.gov.hmcts.opal.repository.EnforcerRepository;
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
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B_1_1;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B_1_1_ENABLED_PROPERTY;

@Service
@Slf4j(topic = "opal.OpalDefendantAccountService")
@RequiredArgsConstructor
public class OpalDefendantAccountEnforcementService
    implements DefendantAccountEnforcementServiceInterface {
    private static final String REASON = "reason";
    private static final String JAIL_DAYS = "jail_days";
    private static final String DAYS_IN_DEFAULT = "daysindefault";
    private static final String ENFORCER_ID = "enforcer_id";
    private static final String ENFORCER = "enforcer";
    private static final String EARLIEST_RELEASE_DATE = "earliest_release_date";
    private static final String EARLIESTRELEASEDATE = "earliestreleasedate";
    private static final String COURT_CODE = "courtcode";
    private static final String HEARING_DATE = "hearingdate";

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

    private final EnforcementPaymentTermsMapper enforcementPaymentTermsMapper;

    private final CourtRepository courtRepository;

    private final EnforcerRepository enforcerRepository;

    private final FeatureToggleApi featureToggleApi;

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
        Long hearingCourtId = null;
        LocalDateTime hearingDate = null;
        List<EnforcementResultResponseDefendantAccount> enforcementResultResponses = request != null
            && request.getEnforcementResultResponses() != null ? request.getEnforcementResultResponses() : List.of();

        Set<String> resultParameterNames = enforcementResultResponses.stream()
            .map(EnforcementResultResponseDefendantAccount::getParameterName)
            .collect(Collectors.toSet());
        for (EnforcementResultResponseDefendantAccount result : enforcementResultResponses) {
            if (featureToggleApi.isFeatureEnabledWithPropertyValueDefault(
                RELEASE_1B_1_1, RELEASE_1B_1_1_ENABLED_PROPERTY, false)) {
                if ((result.getParameterName().equals(JAIL_DAYS) && resultParameterNames.contains(DAYS_IN_DEFAULT))
                    || (result.getParameterName().equals(ENFORCER_ID) && resultParameterNames.contains(ENFORCER))
                    || (result.getParameterName().equals(EARLIEST_RELEASE_DATE)
                    && resultParameterNames.contains(EARLIESTRELEASEDATE))) {
                    log.info("Skipping {} result response parameter, multiple params found", result.getParameterName());
                } else {
                    switch (result.getParameterName()) {
                        case REASON -> reason = result.getResponse();
                        case JAIL_DAYS, DAYS_IN_DEFAULT -> jailDays = Integer.valueOf(result.getResponse());
                        case ENFORCER_ID -> enforcerId = Long.valueOf(result.getResponse());
                        case ENFORCER -> {
                            Optional<EnforcerEntity> enforcer = enforcerRepository
                                .findByEnforcerCodeAndBusinessUnit_businessUnitId(Short.valueOf(
                                    result.getResponse()), businessUnitId);
                            if (enforcer.isPresent()) {
                                enforcerId = enforcer.get().getEnforcerId();
                            } else {
                                log.warn("Enforcer code {} doesn't exist for business unit {}",
                                    result.getResponse(), businessUnitId);
                            }
                        }
                        case EARLIEST_RELEASE_DATE, EARLIESTRELEASEDATE ->
                            earliestReleaseDate = LocalDateTime.parse(result.getResponse());
                        case COURT_CODE -> {
                            Optional<CourtEntity> court = courtRepository
                                .findByCourtCodeAndBusinessUnitId(Short.valueOf(result.getResponse()), businessUnitId);
                            if (court.isPresent()) {
                                hearingCourtId = court.get().getCourtId();
                            } else {
                                log.warn("Court code {} doesn't exist for business unit {}",
                                    result.getResponse(), businessUnitId);
                            }
                        }
                        case HEARING_DATE -> hearingDate = LocalDate.parse(result.getResponse()).atStartOfDay();
                    }
                }
            } else {
                if (Objects.equals(result.getParameterName(), REASON)) {
                    reason = result.getResponse();
                }
                if (Objects.equals(result.getParameterName(), JAIL_DAYS)) {
                    jailDays = Integer.valueOf(result.getResponse());
                }
                if (Objects.equals(result.getParameterName(), ENFORCER_ID)) {
                    enforcerId = Long.valueOf(result.getResponse());
                }
                if (Objects.equals(result.getParameterName(), EARLIEST_RELEASE_DATE)) {
                    earliestReleaseDate = LocalDateTime.parse(result.getResponse());
                }
                if (Objects.equals(result.getParameterName(), COURT_CODE)) {
                    Optional<CourtEntity> court = courtRepository
                        .findByCourtCodeAndBusinessUnitId(Short.valueOf(result.getResponse()), businessUnitId);
                    if (court.isPresent()) {
                        hearingCourtId = court.get().getCourtId();
                    }
                }
                if (Objects.equals(result.getParameterName(), HEARING_DATE)) {
                    hearingDate = LocalDate.parse(result.getResponse()).atStartOfDay();
                }
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
            hearingCourtId,
            hearingDate,
            hearingCourtId,
            hearingDate,
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
                AddPaymentTermsRequestDefendantAccount.builder()
                    .paymentTerms(enforcementPaymentTermsMapper.toPaymentTerms(enforcementPaymentTerms))
                    .requestPaymentCard(false)
                    .generatePaymentTermsChangeLetter(false)
                    .build()
            );
        }

        DefendantAccountEntity latestDefendant = defendantAccountRepositoryService.findById(defendantAccountId);

        return AddEnforcementResponseDefendantAccount.builder()
            .defendantAccountId(String.valueOf(defendantAccountId))
            .version(BigInteger.valueOf(latestDefendant.getVersionNumber()))
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

    @Override
    @Transactional
    public RemoveEnforcementHoldResponseDefendantAccount removeEnforcementHold(
        Long defendantAccountId,
        Short businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        RemoveEnforcementHoldRequestDefendantAccount request) {

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
            AssociatedRecordType.DEFENDANT_ACCOUNTS
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
            AssociatedRecordType.DEFENDANT_ACCOUNTS,
            businessUnitId,
            businessUnitUserId,
            userState.getUserName(),
            null,
            "Remove Enforcement Hold"
        );

        return RemoveEnforcementHoldResponseDefendantAccount.builder()
            .defendantAccountId(String.valueOf(savedEntity.getDefendantAccountId()))
            .version(savedEntity.getVersion())
            .build();
    }

    private AddNoteRequestNotes buildRemoveEnforcementHoldNoteRequest(
        Long defendantAccountId,
        RemoveEnforcementHoldRequestDefendantAccount request) {

        NoteCommon note = NoteCommon.builder()
            .recordType(NoteCommon.RecordTypeEnum.DEFENDANT_ACCOUNTS)
            .recordId(String.valueOf(defendantAccountId))
            .noteText(request.getReason())
            .noteType(NoteCommon.NoteTypeEnum.AA)
            .build();

        return AddNoteRequestNotes.builder().activityNote(note).build();
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
