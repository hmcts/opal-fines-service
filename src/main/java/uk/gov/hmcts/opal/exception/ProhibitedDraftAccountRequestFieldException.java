package uk.gov.hmcts.opal.exception;

public class ProhibitedDraftAccountRequestFieldException extends JsonSchemaValidationException {

    public ProhibitedDraftAccountRequestFieldException(String message) {
        super(message);
    }
}
