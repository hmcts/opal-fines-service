package uk.gov.hmcts.opal.exception;

import java.util.List;
import lombok.Getter;

@Getter
public class UnsupportedMappingTypeException extends RuntimeException {

    private final String mappingType;
    private final List<String> supportedTypes;

    public UnsupportedMappingTypeException(String mappingType, List<String> supportedTypes) {
        super(buildMessage(mappingType, supportedTypes));
        this.mappingType = mappingType;
        this.supportedTypes = List.copyOf(supportedTypes);
    }

    private static String buildMessage(String mappingType, List<String> supportedTypes) {
        return "Unsupported mapping type: " + mappingType
            + ". Supported types: " + String.join(", ", supportedTypes);
    }
}
