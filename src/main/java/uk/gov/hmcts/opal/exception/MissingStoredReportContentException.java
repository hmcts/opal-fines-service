package uk.gov.hmcts.opal.exception;

import java.util.UUID;

public class MissingStoredReportContentException extends RuntimeException {

    public MissingStoredReportContentException(UUID location) {
        super("Stored report content file '" + location + "' was not found");
    }

    public MissingStoredReportContentException(Long reportInstanceId, UUID location) {
        super("Stored report content file '" + location + "' was not found for report instance id: "
            + reportInstanceId);
    }
}
