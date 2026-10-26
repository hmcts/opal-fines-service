package uk.gov.hmcts.opal.service;

public interface DraftAccountReferenceValidationService {

    void validateReferences(Short businessUnitId, String accountJson);
}
