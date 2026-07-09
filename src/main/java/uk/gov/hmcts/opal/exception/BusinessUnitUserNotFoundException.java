package uk.gov.hmcts.opal.exception;

import lombok.Getter;

@Getter
public class BusinessUnitUserNotFoundException extends RuntimeException {

    private final Short businessUnitId;

    public BusinessUnitUserNotFoundException(Short businessUnitId) {
        super("User does not have a business unit user for business unit: " + businessUnitId);
        this.businessUnitId = businessUnitId;
    }
}
