package uk.gov.hmcts.opal.exception;

import lombok.Getter;

@Getter
public class SubmitterDeniedException extends RuntimeException {

    private String submitterUsername;
    private String updateType;

    public SubmitterDeniedException(String submitterUsername, String updateType) {
        super("A single user cannot submit and " + updateType + " the same Draft Account");
        this.submitterUsername = submitterUsername;
        this.updateType = updateType;
    }
}
