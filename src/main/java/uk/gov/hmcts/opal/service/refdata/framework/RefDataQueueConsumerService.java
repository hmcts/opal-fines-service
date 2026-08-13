package uk.gov.hmcts.opal.service.refdata.framework;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j(topic = "opal.RefDataQueueConsumer")
public class RefDataQueueConsumerService {

    private static final String REF_DATA_UPDATE_MESSAGE_SCHEMA = "ref-data/RefDataUpdateMessage.json";

    private final ObjectMapper objectMapper;
    private final SchemaValidationService schemaValidationService;
    private final Map<String, RefDataUpdateHandler<?, ?>> handlersByType;

    public RefDataQueueConsumerService(ObjectMapper objectMapper,
        SchemaValidationService schemaValidationService,
        List<RefDataUpdateHandler<?, ?>> handlers) {
        this.objectMapper = objectMapper;
        this.schemaValidationService = schemaValidationService;
        this.handlersByType = handlers.stream()
            .collect(Collectors.toMap(RefDataUpdateHandler::refDataType, Function.identity()));
    }

    @Transactional
    public void processMessage(String messagePayload) {
        try {
            JsonNode messageNode = readMessageNode(messagePayload);
            schemaValidationService.validateOrError(messageNode, REF_DATA_UPDATE_MESSAGE_SCHEMA);

            RefDataUpdateHandler<?, ?> handler = resolveHandler(messageNode);
            applyUpdate(handler, messageNode.get("payload"));
        } catch (IllegalArgumentException | JsonSchemaValidationException ex) {
            log.warn("Ref-data message will be retried or sent to DLQ: {}", ex.getMessage());
            log.debug("Invalid ref-data message payload was:\n{}", messagePayload, ex);
            throw ex;
        }
    }

    private JsonNode readMessageNode(String messagePayload) {
        try {
            return objectMapper.readTree(messagePayload);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to parse ref data message", ex);
        }
    }

    private RefDataUpdateHandler<?, ?> resolveHandler(JsonNode messageNode) {
        JsonNode refDataTypeNode = messageNode.get("refDataType");
        String refDataType = refDataTypeNode == null ? null : refDataTypeNode.asText();
        RefDataUpdateHandler<?, ?> handler = handlersByType.get(refDataType);
        if (handler == null) {
            throw new IllegalArgumentException("Unknown ref data type: " + refDataType);
        }
        return handler;
    }

    @SuppressWarnings("unchecked")
    private <T, E> void applyUpdate(RefDataUpdateHandler<T, E> handler, JsonNode payloadNode) {
        T dto;
        try {
            dto = objectMapper.convertValue(payloadNode, handler.payloadType());
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Unable to convert ref data payload", ex);
        }
        handler.validateDto(dto);
        E entity = handler.findEntity(dto)
            .orElseGet(() -> handler.createEntity(dto));
        handler.mapper().updateEntityFromDto(dto, entity);
        handler.saveEntity(entity); //this could be a newly created entity
    }
}
