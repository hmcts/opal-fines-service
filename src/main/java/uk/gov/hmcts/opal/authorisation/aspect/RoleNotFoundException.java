package uk.gov.hmcts.opal.authorisation.aspect;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(String message) {
        super(message);
    }
}
