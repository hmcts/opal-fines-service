package uk.gov.hmcts.opal.service.reference.validation;

import static uk.gov.hmcts.opal.util.JsonPathUtil.safeReadList;
import static uk.gov.hmcts.opal.util.JsonPathUtil.safeReadLong;
import static uk.gov.hmcts.opal.util.JsonPathUtil.safeReadString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.result.ImpositionCreditor;
import uk.gov.hmcts.opal.entity.result.ResultLegacyEntity;
import uk.gov.hmcts.opal.repository.CourtLiteRepository;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.OffenceRepository;
import uk.gov.hmcts.opal.repository.ResultLegacyRepository;
import uk.gov.hmcts.opal.util.JsonPathUtil;
import uk.gov.hmcts.opal.util.JsonPathUtil.DocContext;

@Service
@RequiredArgsConstructor
public class LegacyDraftAccountReferenceValidationService extends AbstractDraftAccountReferenceValidationService {
    private static final String ROOT_PATH = "$";
    private static final String DOES_NOT_EXIST = " does not exist";
    private static final String RESULT_ID_PREFIX = ".result_id: result id ";
    private static final String IS_NOT_AN_ENFORCEMENT_RESULT = " is not an enforcement result";
    private static final String IS_NOT_AN_ACTIVE_RESULT = " is not an active result";

    private final OpalDraftAccountReferenceValidationService opalService;
    private final CourtLiteRepository courtLiteRepository;
    private final OffenceRepository offenceRepository;
    private final CreditorAccountRepository creditorAccountRepository;
    private final ResultLegacyRepository resultRepository;

    // Created in PO-10909 - Subsequent tickets will move each method over to use legacy repositories incrementally

    @Override
    void validateEnforcementCourt(DocContext docContext, List<String> failures) {
        opalService.validateEnforcementCourt(docContext, failures);
    }

    @Override
    void validateOffences(Short businessUnitId, JsonPathUtil.DocContext docContext, List<String> failures) {
        List<?> offences = safeReadList(docContext, ROOT_PATH + ".offences");
        if (offences == null) {
            return;
        }
        Map<String, Optional<ResultLegacyEntity>> resultCache = new HashMap<>();
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
                Optional<ResultLegacyEntity> result = findResult(resultId, resultCache);
                if (resultId != null && result.isEmpty()) {
                    failures.add(impositionPath + ".result_id: result id " + resultId + DOES_NOT_EXIST);
                    continue;
                }
                result.ifPresent(resultEntity -> {
                    if (validateImpositionResult(resultId, impositionPath + ".result_id", resultEntity, failures)) {
                        validateImpositionCreditor(
                            businessUnitId,
                            docContext,
                            impositionPath,
                            resultEntity.getImpositionCreditor(),
                            failures);
                    }
                });
            }
        }
    }

    @Override
    void validatePaymentTermsEnforcements(
        JsonPathUtil.DocContext docContext,
        List<String> failures) {
        Map<String, Optional<ResultLegacyEntity>> resultCache = new HashMap<>();
        List<?> enforcements = safeReadList(docContext, ROOT_PATH + ".payment_terms.enforcements");
        if (enforcements == null) {
            return;
        }

        for (int enforcementIndex = 0; enforcementIndex < enforcements.size(); enforcementIndex++) {
            String enforcementPath = ROOT_PATH + ".payment_terms.enforcements[" + enforcementIndex + "]";

            String resultId = safeReadString(docContext, enforcementPath + ".result_id", null);
            if (resultId != null) {
                Optional<ResultLegacyEntity> result = findResult(resultId, resultCache);
                if (result.isEmpty()) {
                    failures.add(enforcementPath + RESULT_ID_PREFIX + resultId + DOES_NOT_EXIST);
                } else if (!result.get().isEnforcement()) {
                    failures.add(enforcementPath + RESULT_ID_PREFIX + resultId + IS_NOT_AN_ENFORCEMENT_RESULT);
                } else if (!result.get().isActive()) {
                    failures.add(enforcementPath + RESULT_ID_PREFIX + resultId + IS_NOT_AN_ACTIVE_RESULT);
                }
            }
        }
    }

    private Optional<ResultLegacyEntity> findResult(
        String resultId, Map<String, Optional<ResultLegacyEntity>> resultCache) {
        if (resultId == null) {
            return Optional.empty();
        }
        return resultCache.computeIfAbsent(resultId, resultRepository::findById);
    }

    private boolean validateImpositionResult(
        String resultId,
        String resultPath,
        ResultLegacyEntity result,
        List<String> failures
    ) {
        boolean valid = true;

        if (!result.isImposition()) {
            failures.add(resultPath + ": result id " + resultId + " is not an imposition result");
            valid = false;
        }

        if (!result.isActive()) {
            failures.add(resultPath + ": result id " + resultId + " is not active");
            valid = false;
        }

        return valid;
    }

    private void validateImpositionCreditor(
        Short businessUnitId,
        JsonPathUtil.DocContext docContext,
        String impositionPath,
        ImpositionCreditor impositionCreditor,
        List<String> failures) {
        if (businessUnitId == null || impositionCreditor == null) {
            return;
        }

        Long majorCreditorId = safeReadLong(docContext, impositionPath + ".major_creditor_id");
        boolean minorCreditorPresent = docContext.readOrNull(impositionPath + ".minor_creditor") != null;

        switch (impositionCreditor) {
            case CF -> validateCentralFundCreditor(businessUnitId, impositionPath, failures);
            case CPS -> validateProsecutionServiceCreditor(businessUnitId, impositionPath, failures);
            case NOT_CPS -> validateMajorOrMinorCreditor(
                businessUnitId,
                impositionPath,
                majorCreditorId,
                minorCreditorPresent,
                impositionCreditor,
                true,
                failures);
            case ANY -> validateMajorOrMinorCreditor(
                businessUnitId,
                impositionPath,
                majorCreditorId,
                minorCreditorPresent,
                impositionCreditor,
                false,
                failures
            );
        }
    }

    private void validateCentralFundCreditor(Short businessUnitId, String impositionPath, List<String> failures) {
        if (!creditorAccountRepository.existsByBusinessUnitIdAndCreditorAccountType(
            businessUnitId,
            CreditorAccountType.CF)) {
            failures.add(
                impositionPath + ".major_creditor_id: no central fund creditor account exists for business unit "
                    + businessUnitId);
        }
    }

    private void validateProsecutionServiceCreditor(
        Short businessUnitId,
        String impositionPath,
        List<String> failures) {
        if (!creditorAccountRepository.existsByBusinessUnitIdAndCreditorAccountTypeAndProsecutionService(
            businessUnitId,
            CreditorAccountType.MJ,
            true)) {
            failures.add(
                impositionPath
                    + ".major_creditor_id: no prosecution service creditor account exists for business unit "
                    + businessUnitId);
        }
    }

    private void validateMajorOrMinorCreditor(
        Short businessUnitId,
        String impositionPath,
        Long majorCreditorId,
        boolean minorCreditorPresent,
        ImpositionCreditor impositionCreditor,
        boolean excludeProsecutionService,
        List<String> failures) {
        if (majorCreditorId == null) {
            if (!minorCreditorPresent) {
                failures.add(
                    impositionPath + ".minor_creditor: a minor creditor or valid major creditor id is required for "
                        + "result creditor rule " + impositionCreditor.getLabel());
            }
            return;
        }

        boolean creditorAccountExists = excludeProsecutionService
            ? creditorAccountRepository
            .existsByBusinessUnitIdAndCreditorAccountTypeAndProsecutionServiceAndMajorCreditorId(
                businessUnitId,
                CreditorAccountType.MJ,
                false,
                majorCreditorId)
            : creditorAccountRepository.existsByBusinessUnitIdAndCreditorAccountTypeAndMajorCreditorId(
                businessUnitId,
                CreditorAccountType.MJ,
                majorCreditorId);

        if (!creditorAccountExists) {
            failures.add(
                impositionPath + ".major_creditor_id: major creditor id " + majorCreditorId
                    + " is not valid for business unit " + businessUnitId
                    + " and result creditor rule " + impositionCreditor.getLabel());
        }
    }

    @Override
    void validateOriginator(DocContext docContext, List<String> failures) {
        opalService.validateOriginator(docContext, failures);
    }
}
