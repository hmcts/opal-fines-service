package uk.gov.hmcts.opal.service.refdata.framework;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RefDataHandlerRegistry {

    private final Map<String, RefDataUpdateHandler<?, ?>> handlersByType;

    public RefDataHandlerRegistry(List<RefDataUpdateHandler<?, ?>> handlers) {
        this.handlersByType = handlers.stream()
            .collect(Collectors.toMap(
                RefDataUpdateHandler::refDataType,
                Function.identity()
            ));
    }

    public Optional<RefDataUpdateHandler<?, ?>> find(String refDataType) {
        if (refDataType == null || refDataType.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(handlersByType.get(refDataType));
    }
}
