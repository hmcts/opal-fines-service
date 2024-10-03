package uk.gov.hmcts.opal.authorisation.aspect;

public class BusinessUnitUserPermissionsNotFoundException extends RuntimeException {

    public BusinessUnitUserPermissionsNotFoundException(String message) {
        super(message);
    }
}
