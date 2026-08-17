package uk.gov.hmcts.opal.service;

import static uk.gov.hmcts.opal.util.JsonPathUtil.createDocContext;
import static uk.gov.hmcts.opal.util.JsonPathUtil.safeReadList;
import static uk.gov.hmcts.opal.util.JsonPathUtil.safeReadLong;
import static uk.gov.hmcts.opal.util.JsonPathUtil.safeReadString;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaType;
import uk.gov.hmcts.opal.entity.ProsecutorEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.OriginatorType;
import uk.gov.hmcts.opal.entity.draft.DraftAccountType;
import uk.gov.hmcts.opal.exception.InvalidReferenceValidationException;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.repository.CourtLiteRepository;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.repository.MajorCreditorRepository;
import uk.gov.hmcts.opal.repository.OffenceRepository;
import uk.gov.hmcts.opal.repository.ProsecutorRepository;
import uk.gov.hmcts.opal.repository.ResultRepository;
import uk.gov.hmcts.opal.util.JsonPathUtil;

@Service
@RequiredArgsConstructor
public class DraftAccountReferenceValidationService {

    private static final String ROOT_PATH = "$";
    private static final String ACCOUNT_TYPE_PATH = ROOT_PATH + ".account_type";
    private static final String ORIGINATOR_ID_PATH = ROOT_PATH + ".originator_id";
    private static final String ORIGINATOR_NAME_PATH = ROOT_PATH + ".originator_name";
    private static final String ORIGINATOR_TYPE_PATH = ROOT_PATH + ".originator_type";
    private static final String DOES_NOT_EXIST = " does not exist";
    private static final String RESULT_ID_PREFIX = ".result_id: result id ";
    private static final String IS_NOT_AN_ENFORCEMENT_RESULT = " is not an enforcement result";
    private static final String IS_NOT_AN_ACTIVE_RESULT = " is not an active result";
    private static final Set<LocalJusticeAreaType> VALID_ORIGINATOR_LJA_TYPES =
        Set.of(LocalJusticeAreaType.LJA, LocalJusticeAreaType.CRWCRT);

    private final CourtLiteRepository courtLiteRepository;
    private final OffenceRepository offenceRepository;
    private final ResultRepository resultRepository;
    private final MajorCreditorRepository majorCreditorRepository;
    private final LocalJusticeAreaRepository localJusticeAreaRepository;
    private final ProsecutorRepository prosecutorRepository;

    @Transactional(readOnly = true)
    public void validateReferences(String accountJson, Short businessUnitId) {
        JsonPathUtil.DocContext docContext;
        try {
            docContext = createDocContext(accountJson, "DraftAccountReferenceValidationService");
        } catch (IllegalArgumentException ex) {
            throw new JsonSchemaValidationException("Unable to parse draft account JSON: " + ex.getMessage(), ex);
        }

        List<String> failures = new ArrayList<>();

        validateEnforcementCourt(docContext, failures);
        validateOffences(docContext, failures, businessUnitId);
        validatePaymentTermsEnforcements(docContext, failures);
        validateOriginator(docContext, failures);

        if (!failures.isEmpty()) {
            throw new InvalidReferenceValidationException(buildFailureMessage(failures));
        }
    }

    private void validateEnforcementCourt(JsonPathUtil.DocContext docContext, List<String> failures) {
        Long enforcementCourtId = safeReadLong(docContext, ROOT_PATH + ".enforcement_court_id");
        if (enforcementCourtId == null) {
            return;
        }

        if (!courtLiteRepository.existsById(enforcementCourtId)) {
            failures.add(ROOT_PATH + ".enforcement_court_id: court id " + enforcementCourtId + DOES_NOT_EXIST);
        }
    }

    private void validateOffences(JsonPathUtil.DocContext docContext, List<String> failures, Short businessUnitId) {
        List<?> offences = safeReadList(docContext, ROOT_PATH + ".offences");
        if (offences == null) {
            return;
        }

        for (int offenceIndex = 0; offenceIndex < offences.size(); offenceIndex++) {
            String offencePath = ROOT_PATH + ".offences[" + offenceIndex + "]";
            String offenceDisplayPath = "account.offences[" + offenceIndex + "]";

            Long offenceId = safeReadLong(docContext, offencePath + ".offence_id");
            if (offenceId != null
                && !offenceRepository.existsByOffenceIdAvailableToBusinessUnit(offenceId, businessUnitId)) {
                failures.add(offenceDisplayPath + ".offence_id: offence id " + offenceId + DOES_NOT_EXIST);
            }

            Long imposingCourtId = safeReadLong(docContext, offencePath + ".imposing_court_id");
            if (imposingCourtId != null && !courtLiteRepository.existsById(imposingCourtId)) {
                failures.add(offencePath + ".imposing_court_id: court id " + imposingCourtId + DOES_NOT_EXIST);
            }

            List<?> impositions = safeReadList(docContext, offencePath + ".impositions");
            if (impositions == null) {
                continue;
            }

            for (int impositionIndex = 0; impositionIndex < impositions.size(); impositionIndex++) {
                String impositionPath = offencePath + ".impositions[" + impositionIndex + "]";

                String resultId = safeReadString(docContext, impositionPath + ".result_id", null);
                validateImpositionResult(resultId, impositionPath + ".result_id", failures);

                Long majorCreditorId = safeReadLong(docContext, impositionPath + ".major_creditor_id");
                if (majorCreditorId != null && !majorCreditorRepository.existsById(majorCreditorId)) {
                    failures.add(impositionPath + ".major_creditor_id: major creditor id " + majorCreditorId
                        + DOES_NOT_EXIST);
                }
            }
        }
    }

    private void validateImpositionResult(String resultId, String resultPath, List<String> failures) {
        if (resultId == null) {
            return;
        }

        ResultEntity result = resultRepository.findById(resultId).orElse(null);
        if (result == null) {
            failures.add(resultPath + ": result id " + resultId + DOES_NOT_EXIST);
            return;
        }

        if (!result.isImposition()) {
            failures.add(resultPath + ": result id " + resultId + " is not an imposition result");
        }

        if (!result.isActive()) {
            failures.add(resultPath + ": result id " + resultId + " is not active");
        }
    }

    private void validatePaymentTermsEnforcements(JsonPathUtil.DocContext docContext, List<String> failures) {
        List<?> enforcements = safeReadList(docContext, ROOT_PATH + ".payment_terms.enforcements");
        if (enforcements == null) {
            return;
        }

        for (int enforcementIndex = 0; enforcementIndex < enforcements.size(); enforcementIndex++) {
            String enforcementPath = ROOT_PATH + ".payment_terms.enforcements[" + enforcementIndex + "]";

            String resultId = safeReadString(docContext, enforcementPath + ".result_id", null);
            if (resultId != null) {
                ResultEntity result = resultRepository.findById(resultId).orElse(null);
                if (result == null) {
                    failures.add(enforcementPath + RESULT_ID_PREFIX + resultId + DOES_NOT_EXIST);
                } else if (!result.isEnforcement()) {
                    failures.add(enforcementPath + RESULT_ID_PREFIX + resultId + IS_NOT_AN_ENFORCEMENT_RESULT);
                } else if (!result.isActive()) {
                    failures.add(enforcementPath + RESULT_ID_PREFIX + resultId + IS_NOT_AN_ACTIVE_RESULT);
                }
            }
        }
    }

    private void validateOriginator(JsonPathUtil.DocContext docContext, List<String> failures) {
        Long originatorId = safeReadLong(docContext, ORIGINATOR_ID_PATH);
        String originatorName = safeReadString(docContext, ORIGINATOR_NAME_PATH, null);
        DraftAccountType accountType = readDraftAccountType(docContext, failures);
        OriginatorType originatorType = readOriginatorType(docContext, failures);

        if (originatorId == null || originatorName == null || accountType == null || originatorType == null) {
            return;
        }

        OriginatorReferenceSource source = resolveOriginatorReferenceSource(originatorType, accountType);
        if (source == null) {
            failures.add(ORIGINATOR_TYPE_PATH + ": unsupported originator/account type combination: originator_type "
                + originatorType.getLabel() + ", account_type " + accountType.getLabel());
            return;
        }

        if (source == OriginatorReferenceSource.LOCAL_JUSTICE_AREA) {
            validateLocalJusticeAreaOriginator(originatorId, originatorName, failures);
            return;
        }

        validateProsecutorOriginator(originatorId, originatorName, failures);
    }

    private void validateLocalJusticeAreaOriginator(Long originatorId, String originatorName, List<String> failures) {
        Short localJusticeAreaId = toShort(originatorId);
        if (localJusticeAreaId == null) {
            failures.add(ORIGINATOR_ID_PATH + ": local justice area id " + originatorId + DOES_NOT_EXIST);
            return;
        }

        LocalJusticeAreaEntity localJusticeArea = localJusticeAreaRepository.findById(localJusticeAreaId)
            .filter(entity -> VALID_ORIGINATOR_LJA_TYPES.contains(entity.getLjaType()))
            .orElse(null);

        if (localJusticeArea == null) {
            failures.add(ORIGINATOR_ID_PATH + ": local justice area id " + originatorId + DOES_NOT_EXIST);
            return;
        }

        if (!originatorName.equals(localJusticeArea.getName())) {
            failures.add(ORIGINATOR_NAME_PATH + ": originator name '" + originatorName
                + "' does not match local justice area name '" + localJusticeArea.getName()
                + "' for id " + originatorId);
        }
    }

    private void validateProsecutorOriginator(Long originatorId, String originatorName, List<String> failures) {
        ProsecutorEntity prosecutor = prosecutorRepository.findById(originatorId).orElse(null);
        if (prosecutor == null) {
            failures.add(ORIGINATOR_ID_PATH + ": prosecutor id " + originatorId + DOES_NOT_EXIST);
            return;
        }

        if (!originatorName.equals(prosecutor.getName())) {
            failures.add(ORIGINATOR_NAME_PATH + ": originator name '" + originatorName
                + "' does not match prosecutor name '" + prosecutor.getName() + "' for id " + originatorId);
        }
    }

    private DraftAccountType readDraftAccountType(JsonPathUtil.DocContext docContext, List<String> failures) {
        String accountTypeLabel = safeReadString(docContext, ACCOUNT_TYPE_PATH, null);
        if (accountTypeLabel == null) {
            return null;
        }

        try {
            return DraftAccountType.getByLabel(accountTypeLabel);
        } catch (IllegalArgumentException ignored) {
            failures.add(ACCOUNT_TYPE_PATH + ": unsupported account type '" + accountTypeLabel + "'");
            return null;
        }
    }

    private OriginatorType readOriginatorType(JsonPathUtil.DocContext docContext, List<String> failures) {
        String originatorTypeLabel = safeReadString(docContext, ORIGINATOR_TYPE_PATH, null);
        if (originatorTypeLabel == null) {
            return null;
        }

        try {
            return OriginatorType.getByLabel(originatorTypeLabel);
        } catch (IllegalArgumentException ignored) {
            failures.add(ORIGINATOR_TYPE_PATH + ": unsupported originator type '" + originatorTypeLabel + "'");
            return null;
        }
    }

    private OriginatorReferenceSource resolveOriginatorReferenceSource(OriginatorType originatorType,
                                                                       DraftAccountType accountType) {
        if (originatorType == OriginatorType.MAC_NEW_ACCOUNT && accountType == DraftAccountType.FINE) {
            return OriginatorReferenceSource.LOCAL_JUSTICE_AREA;
        }

        if (originatorType == OriginatorType.MAC_NEW_ACCOUNT
            && accountType == DraftAccountType.CONDITIONAL_CAUTION) {
            return OriginatorReferenceSource.PROSECUTOR;
        }

        if (originatorType == OriginatorType.FIXED_PENALTY && accountType == DraftAccountType.FIXED_PENALTY) {
            return OriginatorReferenceSource.PROSECUTOR;
        }

        return null;
    }

    private Short toShort(Long value) {
        if (value == null || value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            return null;
        }
        return value.shortValue();
    }

    private String buildFailureMessage(List<String> failures) {
        StringBuilder message = new StringBuilder("Draft account reference validation failed with ")
            .append(failures.size())
            .append(" error(s):");
        for (String failure : failures) {
            message.append("\n - ").append(failure);
        }
        return message.toString();
    }

    private enum OriginatorReferenceSource {
        LOCAL_JUSTICE_AREA,
        PROSECUTOR
    }
}
