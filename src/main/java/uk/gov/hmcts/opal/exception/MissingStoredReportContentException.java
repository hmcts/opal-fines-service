package uk.gov.hmcts.opal.exception;

public class MissingStoredReportContentException extends RuntimeException {

    public MissingStoredReportContentException(String location) {
        super("Stored report content file '" + location + "' was not found");
    }

    public MissingStoredReportContentException(Long reportInstanceId, String location) {
        super("Stored report content file '" + location + "' was not found for report instance id: "
            + reportInstanceId);
    }
}
