package uk.gov.hmcts.opal.authorisation.aspect;

public class BusinessUnitUserNotFoundException extends RuntimeException {

    public BusinessUnitUserNotFoundException(String message) {
        super(message);
    }
}
