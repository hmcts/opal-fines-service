package uk.gov.hmcts.opal.service.refdata.framework;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.util.FeatureFlags;

@Slf4j(topic = "opal.RefDataMessageProcessor")
@Service
public class RefDataMessageProcessor {

    private static final String VALCON_REF_DATA_MESSAGE_SCHEMA = "ref-data/ref_data_schema.json";

    private final ObjectMapper objectMapper;
    private final SchemaValidationService schemaValidationService;
    private final RefDataHandlerRegistry handlerRegistry;
    private final FeatureToggleApi featureToggleApi;
    private final CacheManager cacheManager;

    public RefDataMessageProcessor(ObjectMapper objectMapper,
        SchemaValidationService schemaValidationService,
        RefDataHandlerRegistry handlerRegistry,
        FeatureToggleApi featureToggleApi,
        CacheManager cacheManager) {
        this.objectMapper = objectMapper;
        this.schemaValidationService = schemaValidationService;
        this.handlerRegistry = handlerRegistry;
        this.featureToggleApi = featureToggleApi;
        this.cacheManager = cacheManager;
    }

    @Transactional
    public void processMessage(String messagePayload) {
        if (!featureToggleApi.isFeatureEnabledWithPropertyValueDefault(
            FeatureFlags.REF_DATA_MESSAGE_PROCESSING,
            FeatureFlags.REF_DATA_MESSAGE_PROCESSING_ENABLED_PROPERTY,
            false
        )) {
            log.debug("Ignoring ref-data message because feature {} is disabled",
                FeatureFlags.REF_DATA_MESSAGE_PROCESSING);
            return;
        }

        JsonNode messageNode = readMessageNode(messagePayload);

        schemaValidationService.validateOrError(messageNode, VALCON_REF_DATA_MESSAGE_SCHEMA);

        String dataProduct = extractDataProduct(messageNode);

        Optional<RefDataUpdateHandler<?, ?>> maybeHandler = handlerRegistry.find(dataProduct);
        if (maybeHandler.isEmpty()) {
            log.debug("Ignoring ref-data message with no registered handler for type: {}",
                dataProduct);
            return;
        }
        RefDataUpdateHandler<?, ?> handler = maybeHandler.get();

        JsonNode payloadNode = messageNode.path("payload");
        JsonNode recordsNode = payloadNode.path("records");
        if (!recordsNode.isArray()) {
            return;
        }

        recordsNode.forEach(recordNode -> applyUpdate(handler, recordNode));

        List<String> associatedCaches = handler.cachesToClear();
        associatedCaches.forEach(
            this::clearCache
        );
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

    private void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Unable to clear ref-data cache because no cache is registered with name: {}", cacheName);
            return;
        }
        cache.clear();
        log.debug("Cleared ref-data cache: {}", cacheName);
    }
}
