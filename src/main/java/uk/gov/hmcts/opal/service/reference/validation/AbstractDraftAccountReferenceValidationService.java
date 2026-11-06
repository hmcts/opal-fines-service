package uk.gov.hmcts.opal.service.reference.validation;

import static uk.gov.hmcts.opal.util.JsonPathUtil.createDocContext;
import static uk.gov.hmcts.opal.util.JsonPathUtil.safeReadLong;
import static uk.gov.hmcts.opal.util.JsonPathUtil.safeReadString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.defendantaccount.OriginatorType;
import uk.gov.hmcts.opal.entity.draft.DraftAccountType;
import uk.gov.hmcts.opal.entity.result.ResultLegacyEntity;
import uk.gov.hmcts.opal.exception.InvalidReferenceValidationException;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.util.JsonPathUtil;

abstract class AbstractDraftAccountReferenceValidationService implements DraftAccountReferenceValidationService {

    private static final String ROOT_PATH = "$";
    private static final String ACCOUNT_TYPE_PATH = ROOT_PATH + ".account_type";
    private static final String ORIGINATOR_ID_PATH = ROOT_PATH + ".originator_id";
    private static final String ORIGINATOR_NAME_PATH = ROOT_PATH + ".originator_name";
    private static final String ORIGINATOR_TYPE_PATH = ROOT_PATH + ".originator_type";
    private static final String DOES_NOT_EXIST = " does not exist";

    @Transactional(readOnly = true)
    @Override
    public void validateReferences(Short businessUnitId, String accountJson) {
        JsonPathUtil.DocContext docContext;
        try {
            docContext = createDocContext(accountJson, "DraftAccountReferenceValidationService");
        } catch (IllegalArgumentException ex) {
            throw new JsonSchemaValidationException("Unable to parse draft account JSON: " + ex.getMessage(), ex);
        }

        List<String> failures = new ArrayList<>();

        validateEnforcementCourt(docContext, failures);
        validateOffences(businessUnitId, docContext, failures);
        validatePaymentTermsEnforcements(docContext, failures, new HashMap<>());
        validateOriginator(docContext, failures);

        if (!failures.isEmpty()) {
            throw new InvalidReferenceValidationException(buildFailureMessage(failures));
        }
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

    abstract void validateEnforcementCourt(JsonPathUtil.DocContext docContext, List<String> failures);

    abstract void validateOffences(Short businessUnitId, JsonPathUtil.DocContext docContext, List<String> failures);

    abstract void validatePaymentTermsEnforcements(JsonPathUtil.DocContext docContext,
        List<String> failures,
        Map<String, Optional<ResultLegacyEntity>> resultCache);

    void validateOriginator(JsonPathUtil.DocContext docContext, List<String> failures) {
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

    private void validateLocalJusticeAreaOriginator(
        Long originatorId, String originatorName, List<String> failures) {

        Short localJusticeAreaId = toShort(originatorId);
        if (localJusticeAreaId == null) {
            failures.add(ORIGINATOR_ID_PATH + ": local justice area id " + originatorId + DOES_NOT_EXIST);
            return;
        }

        String localJusticeAreaName = findLocalJusticeAreaName(localJusticeAreaId).orElse(null);
        if (localJusticeAreaName == null) {
            failures.add(ORIGINATOR_ID_PATH + ": local justice area id " + originatorId + DOES_NOT_EXIST);
            return;
        }

        if (!originatorName.equals(localJusticeAreaName)) {
            failures.add(ORIGINATOR_NAME_PATH + ": originator name '" + originatorName
                + "' does not match local justice area name '" + localJusticeAreaName + "' for id " + originatorId);
        }
    }

    private void validateProsecutorOriginator(Long originatorId, String originatorName, List<String> failures) {
        String prosecutorName = findProsecutorName(originatorId).orElse(null);
        if (prosecutorName == null) {
            failures.add(ORIGINATOR_ID_PATH + ": prosecutor id " + originatorId + DOES_NOT_EXIST);
            return;
        }

        if (!originatorName.equals(prosecutorName)) {
            failures.add(ORIGINATOR_NAME_PATH + ": originator name '" + originatorName
                + "' does not match prosecutor name '" + prosecutorName + "' for id " + originatorId);
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

    private Short toShort(Long value) {
        if (value == null || value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            return null;
        }
        return value.shortValue();
    }

    abstract Optional<String> findLocalJusticeAreaName(Short localJusticeAreaId);

    abstract Optional<String> findProsecutorName(Long prosecutorId);

    private enum OriginatorReferenceSource {
        LOCAL_JUSTICE_AREA,
        PROSECUTOR
    }
}
