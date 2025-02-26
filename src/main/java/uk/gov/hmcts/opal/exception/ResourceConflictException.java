package uk.gov.hmcts.opal.exception;

import lombok.Getter;

import java.util.Optional;

@Getter
public class ResourceConflictException extends RuntimeException {

    private final String resourceType;
    private final String resourceId;
    private final String conflictReason;

    public ResourceConflictException(String resourceType,  Object resourceId, String conflictReason) {
        this.resourceType = resourceType;
        this.resourceId = Optional.ofNullable(resourceId).map(Object::toString).orElse("<null>");
        this.conflictReason = conflictReason;
    }

}
