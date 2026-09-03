package uk.gov.hmcts.opal.service.refdata.framework;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j(topic = "opal.RefDataMessageProcessor")
@Service
public class RefDataMessageProcessor {

    private static final String VALCON_REF_DATA_MESSAGE_SCHEMA = "ref-data/valcon_oneofpayload.json";

    private final ObjectMapper objectMapper;
    private final SchemaValidationService schemaValidationService;
    private final RefDataHandlerRegistry handlerRegistry;

    public RefDataMessageProcessor(ObjectMapper objectMapper,
        SchemaValidationService schemaValidationService,
        RefDataHandlerRegistry handlerRegistry) {
        this.objectMapper = objectMapper;
        this.schemaValidationService = schemaValidationService;
        this.handlerRegistry = handlerRegistry;
    }

    @Transactional
    public void processMessage(String messagePayload) {
        JsonNode messageNode = readMessageNode(messagePayload);

        schemaValidationService.validateOrError(messageNode, VALCON_REF_DATA_MESSAGE_SCHEMA);

        String dataProduct = extractDataProduct(messageNode);

        Optional<RefDataUpdateHandler<?, ?>> handler = handlerRegistry.find(dataProduct);
        if (handler.isEmpty()) {
            log.debug("Ignoring ref-data message with no registered handler for type: {}",
                dataProduct);
            return;
        }

        JsonNode payloadNode = messageNode.path("payload");
        JsonNode recordsNode = payloadNode.path("records");
        if (!recordsNode.isArray()) {
            return;
        }

        recordsNode.forEach(recordNode -> applyUpdate(handler.get(), recordNode));
    }

    private JsonNode readMessageNode(String messagePayload) {
        try {
            return objectMapper.readTree(messagePayload);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to parse ref data message", ex);
        }
    }

    private String extractDataProduct(JsonNode messageNode) {
        String dataProduct = messageNode.path("header").path("dataProduct").asText(null);
        if (dataProduct == null || dataProduct.isBlank()) {
            dataProduct = messageNode.path("dataProduct").asText(null);
        }
        return dataProduct;
    }

    @SuppressWarnings("unchecked")
    private <T, E> void applyUpdate(RefDataUpdateHandler<T, E> handler, Object payload) {
        T dto;
        try {
            dto = objectMapper.convertValue(payload, handler.payloadType());
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
