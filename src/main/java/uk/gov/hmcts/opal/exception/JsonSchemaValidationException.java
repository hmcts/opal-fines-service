package uk.gov.hmcts.opal.exception;

import java.util.Set;

public class JsonSchemaValidationException extends RuntimeException {

    private final Set<JsonSchemaValidationError> validationErrors;

    public JsonSchemaValidationException(String msg) {
        super(msg);
        this.validationErrors = Set.of();
    }

    public JsonSchemaValidationException(Throwable t) {
        super(t);
        this.validationErrors = Set.of();
    }

    public JsonSchemaValidationException(String message, Throwable t) {
        super(message, t);
        this.validationErrors = Set.of();
    }

    public JsonSchemaValidationException(String message, Set<JsonSchemaValidationError> validationErrors) {
        super(message);
        this.validationErrors = Set.copyOf(validationErrors);
    }

    public Set<JsonSchemaValidationError> getValidationErrors() {
        return validationErrors;
    }

    public record JsonSchemaValidationError(String keyword, String property, String message) {
    }

}
