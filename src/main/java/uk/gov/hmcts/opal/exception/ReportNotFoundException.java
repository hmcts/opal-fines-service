package uk.gov.hmcts.opal.exception;

public class ReportNotFoundException extends RuntimeException {


    public ReportNotFoundException(String message) {
        super(message);
    }

}
