package uk.gov.hmcts.opal.service;

import static uk.gov.hmcts.opal.util.JsonPathUtil.createDocContext;
import static uk.gov.hmcts.opal.util.JsonPathUtil.safeReadList;
import static uk.gov.hmcts.opal.util.JsonPathUtil.safeReadLong;
import static uk.gov.hmcts.opal.util.JsonPathUtil.safeReadString;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.repository.CourtLiteRepository;
import uk.gov.hmcts.opal.repository.MajorCreditorRepository;
import uk.gov.hmcts.opal.repository.OffenceRepository;
import uk.gov.hmcts.opal.repository.ResultRepository;
import uk.gov.hmcts.opal.util.JsonPathUtil;

@Service
@RequiredArgsConstructor
public class DraftAccountReferenceValidationService {

    private static final String ROOT_PATH = "$";

    private final CourtLiteRepository courtLiteRepository;
    private final OffenceRepository offenceRepository;
    private final ResultRepository resultRepository;
    private final MajorCreditorRepository majorCreditorRepository;

    @Transactional(readOnly = true)
    public void validateReferences(String accountJson) {
        JsonPathUtil.DocContext docContext;
        try {
            docContext = createDocContext(accountJson, "DraftAccountReferenceValidationService");
        } catch (IllegalArgumentException ex) {
            throw new JsonSchemaValidationException("Unable to parse draft account JSON: " + ex.getMessage(), ex);
        }

        List<String> failures = new ArrayList<>();

        validateEnforcementCourt(docContext, failures);
        validateOffences(docContext, failures);
        validatePaymentTermsEnforcements(docContext, failures);

        if (!failures.isEmpty()) {
            throw new JsonSchemaValidationException(buildFailureMessage(failures));
        }
    }

    private void validateEnforcementCourt(JsonPathUtil.DocContext docContext, List<String> failures) {
        Long enforcementCourtId = safeReadLong(docContext, ROOT_PATH + ".enforcement_court_id");
        if (enforcementCourtId == null) {
            return;
        }

        if (!courtLiteRepository.existsById(enforcementCourtId)) {
            failures.add(ROOT_PATH + ".enforcement_court_id: court id " + enforcementCourtId + " does not exist");
        }
    }

    private void validateOffences(JsonPathUtil.DocContext docContext, List<String> failures) {
        List<?> offences = safeReadList(docContext, ROOT_PATH + ".offences");
        if (offences == null) {
            return;
        }

        for (int offenceIndex = 0; offenceIndex < offences.size(); offenceIndex++) {
            String offencePath = ROOT_PATH + ".offences[" + offenceIndex + "]";

            Long offenceId = safeReadLong(docContext, offencePath + ".offence_id");
            if (offenceId != null && !offenceRepository.existsById(offenceId)) {
                failures.add(offencePath + ".offence_id: offence id " + offenceId + " does not exist");
            }

            Long imposingCourtId = safeReadLong(docContext, offencePath + ".imposing_court_id");
            if (imposingCourtId != null && !courtLiteRepository.existsById(imposingCourtId)) {
                failures.add(offencePath + ".imposing_court_id: court id " + imposingCourtId + " does not exist");
            }

            List<?> impositions = safeReadList(docContext, offencePath + ".impositions");
            if (impositions == null) {
                continue;
            }

            for (int impositionIndex = 0; impositionIndex < impositions.size(); impositionIndex++) {
                String impositionPath = offencePath + ".impositions[" + impositionIndex + "]";

                String resultId = safeReadString(docContext, impositionPath + ".result_id", null);
                if (resultId != null && !resultRepository.existsById(resultId)) {
                    failures.add(impositionPath + ".result_id: result id " + resultId + " does not exist");
                }

                Long majorCreditorId = safeReadLong(docContext, impositionPath + ".major_creditor_id");
                if (majorCreditorId != null && !majorCreditorRepository.existsById(majorCreditorId)) {
                    failures.add(impositionPath + ".major_creditor_id: major creditor id " + majorCreditorId
                        + " does not exist");
                }
            }
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
            if (resultId != null && !resultRepository.existsById(resultId)) {
                failures.add(enforcementPath + ".result_id: result id " + resultId + " does not exist");
            }
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
}
