package uk.gov.hmcts.opal.service.messaging;

public class TillNotProcessingException extends RuntimeException {

    public TillNotProcessingException(Long tillId) {
        super("Till " + tillId + " is not PROCESSING");
    }
}
