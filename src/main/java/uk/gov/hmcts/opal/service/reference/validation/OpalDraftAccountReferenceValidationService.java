package uk.gov.hmcts.opal.service.reference.validation;

import static uk.gov.hmcts.opal.util.JsonPathUtil.safeReadLong;
import static uk.gov.hmcts.opal.util.JsonPathUtil.safeReadString;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.OpalLocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.OpalLocalJusticeAreaType;
import uk.gov.hmcts.opal.entity.ProsecutorOpalEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.OriginatorType;
import uk.gov.hmcts.opal.entity.draft.DraftAccountType;
import uk.gov.hmcts.opal.repository.CourtLiteRepository;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.OpalLocalJusticeAreaRepository;
import uk.gov.hmcts.opal.repository.OffenceRepository;
import uk.gov.hmcts.opal.repository.OpalProsecutorRepository;
import uk.gov.hmcts.opal.repository.ResultRepository;
import uk.gov.hmcts.opal.util.JsonPathUtil;

@Service
@RequiredArgsConstructor
public class OpalDraftAccountReferenceValidationService extends AbstractDraftAccountReferenceValidationService {

    private static final String ROOT_PATH = "$";
    private static final String ACCOUNT_TYPE_PATH = ROOT_PATH + ".account_type";
    private static final String ORIGINATOR_ID_PATH = ROOT_PATH + ".originator_id";
    private static final String ORIGINATOR_NAME_PATH = ROOT_PATH + ".originator_name";
    private static final String ORIGINATOR_TYPE_PATH = ROOT_PATH + ".originator_type";
    private static final String ENFORCEMENT_COURT_ID_PATH = ROOT_PATH + ".enforcement_court_id";
    private static final String ENFORCEMENT_COURT_ID_ERROR_PATH = "account.enforcement_court_id";
    private static final String DOES_NOT_EXIST = " does not exist";
    private static final Set<OpalLocalJusticeAreaType> VALID_ORIGINATOR_LJA_TYPES =
        Set.of(OpalLocalJusticeAreaType.LJA, OpalLocalJusticeAreaType.CRWCRT);

    private final CourtLiteRepository courtLiteRepository;
    private final OffenceRepository offenceRepository;
    private final ResultRepository resultRepository;
    private final CreditorAccountRepository creditorAccountRepository;
    private final OpalLocalJusticeAreaRepository opalLocalJusticeAreaRepository;
    private final OpalProsecutorRepository opalProsecutorRepository;


    @Override
    void validateEnforcementCourt(JsonPathUtil.DocContext docContext, List<String> failures) {
        Long enforcementCourtId = safeReadLong(docContext, ENFORCEMENT_COURT_ID_PATH);
        if (enforcementCourtId == null) {
            return;
        }

        if (!courtLiteRepository.existsById(enforcementCourtId)) {
            failures.add(ENFORCEMENT_COURT_ID_ERROR_PATH + ": court id " + enforcementCourtId + DOES_NOT_EXIST);
        }
    }


    @Override
    CourtLiteRepository courtLiteRepository() {
        return courtLiteRepository;
    }

    @Override
    OffenceRepository offenceRepository() {
        return offenceRepository;
    }

    @Override
    CreditorAccountRepository creditorAccountRepository() {
        return creditorAccountRepository;
    }

    @Override
    Optional<ResultView> lookupResult(String resultId) {
        return resultRepository.findById(resultId).map(result -> new ResultView(
            result.isActive(), result.isImposition(), result.isEnforcement(), result.getImpositionCreditor()));
    }

    @Override
    void validateOriginator(JsonPathUtil.DocContext docContext, List<String> failures) {
        validateOriginator(docContext, failures, this::findProsecutorName);
    }

    void validateOriginator(JsonPathUtil.DocContext docContext, List<String> failures,
                            Function<String, Optional<String>> prosecutorNameLookup) {

        String originatorId = safeReadString(docContext, ORIGINATOR_ID_PATH, null);
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

        validateProsecutorOriginator(originatorId, originatorName, failures, prosecutorNameLookup);
    }

    private void validateLocalJusticeAreaOriginator(Long originatorId, String originatorName, List<String> failures) {
        Short localJusticeAreaId = toShort(originatorId);
        if (localJusticeAreaId == null) {
            failures.add(ORIGINATOR_ID_PATH + ": local justice area id " + originatorId + DOES_NOT_EXIST);
            return;
        }

        OpalLocalJusticeAreaEntity localJusticeArea = opalLocalJusticeAreaRepository.findById(localJusticeAreaId)
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

    private void validateLocalJusticeAreaOriginator(String originatorId, String originatorName, List<String> failures) {
        Long localJusticeAreaKey = parseLong(originatorId);
        if (localJusticeAreaKey == null) {
            failures.add(ORIGINATOR_ID_PATH + ": local justice area id " + originatorId + DOES_NOT_EXIST);
            return;
        }
        validateLocalJusticeAreaOriginator(localJusticeAreaKey, originatorName, failures);
    }

    private void validateProsecutorOriginator(String originatorId, String originatorName, List<String> failures,
                                              Function<String, Optional<String>> prosecutorNameLookup) {

        String prosecutorName = prosecutorNameLookup.apply(originatorId).orElse(null);
        if (prosecutorName == null) {
            failures.add(ORIGINATOR_ID_PATH + ": prosecutor id " + originatorId + DOES_NOT_EXIST);
            return;
        }

        if (!originatorName.equals(prosecutorName)) {
            failures.add(ORIGINATOR_NAME_PATH + ": originator name '" + originatorName
                + "' does not match prosecutor name '" + prosecutorName + "' for id " + originatorId);
        }
    }

    private Optional<String> findProsecutorName(String prosecutorCode) {
        if (prosecutorCode == null) {
            return Optional.empty();
        }
        return opalProsecutorRepository.findById(prosecutorCode).map(ProsecutorOpalEntity::getName);
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
        if (originatorType == OriginatorType.MAC_NEW_ACCOUNT) {
            if (accountType == DraftAccountType.FINE || accountType == DraftAccountType.CONFISCATION) {
                return OriginatorReferenceSource.LOCAL_JUSTICE_AREA;
            }
            return accountType == DraftAccountType.CONDITIONAL_CAUTION ? OriginatorReferenceSource.PROSECUTOR : null;
        }

        if (originatorType == OriginatorType.TRANSFER_IN_ACCOUNT) {
            if (accountType == DraftAccountType.FINE || accountType == DraftAccountType.CONFISCATION) {
                return OriginatorReferenceSource.LOCAL_JUSTICE_AREA;
            }
            return accountType == DraftAccountType.FIXED_PENALTY ? OriginatorReferenceSource.PROSECUTOR : null;
        }

        if (originatorType == OriginatorType.FIXED_PENALTY && accountType == DraftAccountType.FIXED_PENALTY) {
            return OriginatorReferenceSource.PROSECUTOR;
        }

        return null;
    }

    Short toShort(Long value) {
        if (value == null || value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            return null;
        }
        return value.shortValue();
    }

    Short toShort(String value) {
        Long parsedValue = parseLong(value);
        return parsedValue == null ? null : toShort(parsedValue);
    }

    private Long parseLong(String value) {
        try {
            return value == null || value.isBlank() ? null : Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private enum OriginatorReferenceSource {
        LOCAL_JUSTICE_AREA,
        PROSECUTOR
    }
}
