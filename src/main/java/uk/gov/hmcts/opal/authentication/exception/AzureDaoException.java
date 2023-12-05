package uk.gov.hmcts.opal.authentication.exception;

import lombok.Getter;

@SuppressWarnings("PMD.MissingSerialVersionUID")
@Getter
public class AzureDaoException extends Exception {

    private int httpStatus;

    public AzureDaoException(String message) {
        super(message);
    }

    public AzureDaoException(String message, Throwable cause) {
        super(message, cause);
    }

    public AzureDaoException(String message, String reason, int httpStatus) {
        super(String.format("%s: %s", message, reason));
        this.httpStatus = httpStatus;
    }

}
