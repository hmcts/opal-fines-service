package uk.gov.hmcts.opal.service;

import org.springframework.transaction.annotation.Transactional;

public interface DraftAccountReferenceValidationService {

    void validateReferences(Short businessUnitId, String accountJson);
}
