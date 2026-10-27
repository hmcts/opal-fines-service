package uk.gov.hmcts.opal.service.reference.validation;

import static uk.gov.hmcts.opal.util.JsonPathUtil.createDocContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.exception.InvalidReferenceValidationException;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.util.JsonPathUtil;

public abstract class AbstractDraftAccountReferenceValidationService implements DraftAccountReferenceValidationService{

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

    abstract void validateEnforcementCourt(JsonPathUtil.DocContext docContext, List<String> failuress);

    abstract void validateOffences(Short businessUnitId, JsonPathUtil.DocContext docContext, List<String> failures);

    abstract void validatePaymentTermsEnforcements(JsonPathUtil.DocContext docContext,
        List<String> failures,
        Map<String, Optional<ResultEntity>> resultCache);

    abstract void validateOriginator(JsonPathUtil.DocContext docContext, List<String> failures);
}
