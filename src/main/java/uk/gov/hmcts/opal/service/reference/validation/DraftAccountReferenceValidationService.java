package uk.gov.hmcts.opal.service.reference.validation;

public interface DraftAccountReferenceValidationService {

    void validateReferences(Short businessUnitId, String accountJson);
}
