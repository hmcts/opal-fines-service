package uk.gov.hmcts.opal.exception;

import java.util.List;
import lombok.Getter;

@Getter
public class MissingMappingTypeException extends RuntimeException {

    private final List<String> supportedTypes;

    public MissingMappingTypeException(List<String> supportedTypes) {
        super(buildMessage(supportedTypes));
        this.supportedTypes = List.copyOf(supportedTypes);
    }

    private static String buildMessage(List<String> supportedTypes) {
        return "Required mapping type is missing. Supported types: " + String.join(", ", supportedTypes);
    }
}
