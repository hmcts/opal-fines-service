package uk.gov.hmcts.opal.service.legacy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.service.DraftAccountReferenceValidationService;
import uk.gov.hmcts.opal.service.opal.OpalDraftAccountReferenceValidationService;

@Service
@RequiredArgsConstructor
public class LegacyDraftAccountReferenceValidationService implements DraftAccountReferenceValidationService {
    private final OpalDraftAccountReferenceValidationService opalService;

    @Override
    public void validateReferences(Short businessUnitId, String accountJson) {
        // Created in PO-10909 - Subsequent tickets will move over to use legacy repositories incrementally
        opalService.validateReferences(businessUnitId, accountJson);
    }
}
