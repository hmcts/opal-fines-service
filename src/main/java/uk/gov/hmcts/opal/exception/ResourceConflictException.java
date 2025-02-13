package uk.gov.hmcts.opal.exception;

import lombok.Getter;

@Getter
public class ResourceConflictException extends RuntimeException {

    private final String resourceType;
    private final String resourceId;
    private final String conflictReason;

    public ResourceConflictException(String resourceType,  String resourceId, String conflictReason) {
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.conflictReason = conflictReason;
    }

}
